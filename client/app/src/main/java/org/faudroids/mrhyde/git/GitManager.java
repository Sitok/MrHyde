package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
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
import org.faudroids.mrhyde.github.GitHubRepository;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Handles "low level" git operations for a single repository.
 */
public class GitManager {

  private final GitHubRepository repository;
  private final Git gitClient;
  private final File rootDir;
  private final FileUtils fileUtils;

  public GitManager(
      @NonNull GitHubRepository repository,
      @NonNull Git gitClient,
      @NonNull File rootDir,
      @NonNull FileUtils fileUtils) {
    this.repository = repository;
    this.gitClient = gitClient;
    this.rootDir = rootDir;
    this.fileUtils = fileUtils;
  }


  public Observable<Void> deleteAllLocalContent() {
    return fileUtils.deleteFile(rootDir);
  }

  public File getRootDir() {
    return rootDir;
  }

  public Observable<Void> commitAllChanges(String commitMsg) {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      gitClient.add().addFilepattern(".").call();
      gitClient.commit().setMessage(commitMsg).call();
      return null;
    });
  }

  public Observable<List<String>> log() {
    return ObservableUtils.fromSynchronousCall(() -> {
      Iterable<RevCommit> logs = gitClient.log().call();
      List<String> result = new ArrayList<>();
      for (RevCommit log : logs) result.add(log.getShortMessage());
      return result;
    });
  }

  public Observable<List<String>> branch() {
    return ObservableUtils.fromSynchronousCall(() -> {
      Iterable<Ref> branches = gitClient.branchList().call();
      List<String> result = new ArrayList<>();
      for (Ref branch : branches) result.add(branch.getName());
      return result;
    });
  }

  public Observable<Void> pull() {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      gitClient.pull().call();
      return null;
    });
  }

  public Observable<Void> push() {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      gitClient.push().call();
      return null;
    });
  }

  public Observable<Void> resetHard() {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      gitClient.reset().setMode(ResetCommand.ResetType.HARD).call();
      gitClient.clean().setCleanDirectories(true).call();
      return null;
    });
  }

  public Observable<Status> status() {
    return ObservableUtils.fromSynchronousCall(() -> gitClient.status().call());
  }

  public Observable<String> diff() {
    return ObservableUtils.fromSynchronousCall(() -> {
      AbstractTreeIterator commitTreeIterator = prepareCommitTreeIterator(gitClient.getRepository());
      FileTreeIterator workTreeIterator = new FileTreeIterator(gitClient.getRepository());

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      DiffFormatter formatter = new DiffFormatter(outputStream);
      formatter.setRepository(gitClient.getRepository());

      List<DiffEntry> diffs = formatter.scan(commitTreeIterator, workTreeIterator);
      /*
      // remove ignored files
      if (filesToIgnore != null) {
        Iterator<DiffEntry> entryIterator = diffs.iterator();
        while (entryIterator.hasNext()) {
          if (filesToIgnore.contains(entryIterator.next().getNewPath())) {
            entryIterator.remove();
          }
        }
      }
      */
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

  public Observable<Void> checkoutBranch(String branchName) {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      gitClient.checkout().setName(branchName).call();
      return null;
    });
  }

  /*
  public static String refToBranchName(String ref) {
    return ref.replace("refs/master/", "");
  }
  */
}
