package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rx.Observable;
import timber.log.Timber;

/**
 * Handles "low level" git operations for a single repository.
 */
public class GitManager {

  private final Repository repository;
  private final Git gitClient;
  private final FileUtils fileUtils;
  private final GitCommandAuthAdapter gitCommandAuthAdapter;
  private final LoginManager loginManager;

  public GitManager(
      @NonNull Repository repository,
      @NonNull Git gitClient,
      @NonNull FileUtils fileUtils,
      @NonNull GitCommandAuthAdapter gitCommandAuthAdapter,
      @NonNull LoginManager loginManager) {
    this.repository = repository;
    this.gitClient = gitClient;
    this.fileUtils = fileUtils;
    this.gitCommandAuthAdapter = gitCommandAuthAdapter;
    this.loginManager = loginManager;
  }


  Observable<Void> deleteAllLocalContent() {
    return fileUtils.deleteFile(repository.getRootDir());
  }

  public Repository getRepository() {
    return repository;
  }

  public Observable<Void> commitAllChanges(String commitMsg) {
    return status()
        .flatMap(status -> ObservableUtils.fromSynchronousCall(() -> {
          // remove git "missing" files (jgit does not have a "git add --all" option)
          Set<String> removedFileNames = status.getMissing();
          if (removedFileNames.size() > 0) {
            RmCommand rmCommand = gitClient.rm();
            for (String removedFileName : removedFileNames) {
              rmCommand = rmCommand.addFilepattern(removedFileName);
            }
            rmCommand.call();
          }

          // add all changed (!= deleted) files
          gitClient.add().addFilepattern(".").call();

          // commit
          Account account = loginManager.getAccount(repository);
          gitClient
              .commit()
              .setMessage(commitMsg)
              .setCommitter(account.getLogin(), account.getEmail())
              .call();

          return null;
        }));
  }

  public Observable<List<String>> log() {
    return ObservableUtils.fromSynchronousCall(() -> {
      Iterable<RevCommit> logs = gitClient.log().call();
      List<String> result = new ArrayList<>();
      for (RevCommit log : logs) result.add(log.getShortMessage());
      return result;
    });
  }

  /**
   * @return all local (!) remote tracking branches. See http://stackoverflow.com/a/24785777
   * for a good explanation.
   */
  public Observable<List<Branch>> listRemoteTrackingBranches() {
    return ObservableUtils.fromSynchronousCall(() -> {
      Iterable<Ref> branches = gitClient
          .branchList()
          .setListMode(ListBranchCommand.ListMode.REMOTE)
          .call();
      List<Branch> result = new ArrayList<>();
      for (Ref ref: branches) result.add(new Branch(ref));
      return result;
    });
  }

  /**
   * @return the display name of this branch without origin.
   */
  public Observable<String> getCurrentBranchName() {
    return ObservableUtils.fromSynchronousCall(() -> gitClient.getRepository().getBranch());
  }

  public Observable<Void> checkoutBranch(Branch branch) {
    return ObservableUtils.fromSynchronousCall(() -> {
      CheckoutCommand checkoutCommand = gitClient.checkout().setName(branch.getDisplayName());

      try {
        checkoutCommand.call();
      } catch (RefNotFoundException e) {
        Timber.i("Branch not found, creating new one");
        checkoutCommand
            .setCreateBranch(true)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
            .call();
      }

      return null;
    });
  }

  public Observable<Void> pull() {
    return ObservableUtils.fromSynchronousCall(() -> {
      gitCommandAuthAdapter.wrap(gitClient.pull()).call();
      return null;
    });
  }

  public Observable<Void> push() {
    return ObservableUtils.fromSynchronousCall(() -> {
      gitCommandAuthAdapter.wrap(gitClient.push()).call();
      return null;
    });
  }

  public Observable<Void> resetHardAndClean() {
    return ObservableUtils.fromSynchronousCall(() -> {
      gitClient.reset().setMode(ResetCommand.ResetType.HARD).call();
      gitClient.clean().setCleanDirectories(true).call();
      return null;
    });
  }

  public Observable<Status> status() {
    return ObservableUtils.fromSynchronousCall(() -> gitClient.status().call());
  }

  public Observable<String> diff() {
    return diff(new HashSet<>());
  }

  public Observable<String> diff(Set<String> filesToIgnore) {
    return ObservableUtils.fromSynchronousCall(() -> {
      AbstractTreeIterator commitTreeIterator = prepareCommitTreeIterator(gitClient.getRepository());
      FileTreeIterator workTreeIterator = new FileTreeIterator(gitClient.getRepository());

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      DiffFormatter formatter = new DiffFormatter(outputStream);
      formatter.setRepository(gitClient.getRepository());

      List<DiffEntry> diffs = formatter.scan(commitTreeIterator, workTreeIterator);
      // remove ignored files
      if (filesToIgnore != null) {
        Iterator<DiffEntry> entryIterator = diffs.iterator();
        while (entryIterator.hasNext()) {
          if (filesToIgnore.contains(entryIterator.next().getNewPath())) {
            entryIterator.remove();
          }
        }
      }
      formatter.format(diffs);
      return outputStream.toString();
    });
  }

  private static AbstractTreeIterator prepareCommitTreeIterator(org.eclipse.jgit.lib.Repository repository) throws IOException, GitAPIException {
    Ref head = repository.getRef(repository.getBranch());
    RevWalk walk = new RevWalk(repository);
    RevCommit commit = walk.parseCommit(head.getObjectId());
    RevTree tree = walk.parseTree(commit.getTree().getId());

    CanonicalTreeParser parser = new CanonicalTreeParser();
    ObjectReader reader = repository.newObjectReader();
    try {
      parser.reset(reader, tree.getId());
    } finally {
      reader.release();
    }
    walk.dispose();
    return parser;
  }

}
