package org.faudroids.mrhyde.git;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.io.Files;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.faudroids.mrhyde.github.LoginManager;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import timber.log.Timber;

/**
 * Creates {@link GitManager} instances.
 */
@Singleton
public class GitManagerFactory {

  private static final String PATH_REPOS_GITHUB = "github";

  private final Context context;
  private final LoginManager loginManager;
  private final FileUtils fileUtils;
  private final GitCommandAuthAdapter gitCommandAuthAdapter;

  @Inject
  public GitManagerFactory(
      Context context,
      LoginManager loginManager,
      FileUtils fileUtils,
      GitCommandAuthAdapter gitCommandAuthAdapter) {
    this.context = context;
    this.loginManager = loginManager;
    this.fileUtils = fileUtils;
    this.gitCommandAuthAdapter = gitCommandAuthAdapter;
  }

  public GitManager openRepository(@NonNull final Repository repository) {
    try {
      File rootDir = getRepoRootDir(repository);
      Git client = Git.open(rootDir);
      return new GitManager(repository, client, rootDir, fileUtils, gitCommandAuthAdapter, loginManager);
    } catch (IOException e) {
      Timber.e(e, "Failed to open local git repository");
      return null;
    }
  }

  public Observable<GitManager> cloneRepository(@NonNull Repository repository, boolean importPreV1Repo) {
    return ObservableUtils.fromSynchronousCall(() -> {
      File rootDir = getRepoRootDir(repository);

      // clone repo
      Git client = gitCommandAuthAdapter.wrap(Git
          .cloneRepository()
          .setURI(repository.getCloneUrl())
          .setDirectory(rootDir))
          .call();

      // copy v1 files
      File preV1RootDir = getPreV1RootDir(repository);
      if (importPreV1Repo) {
        copyFiles(preV1RootDir, rootDir);
      }

      // delete pre v1 files
      if (preV1RootDir.exists()) {
        fileUtils.deleteFile(preV1RootDir).toBlocking().first();
      }

      return new GitManager(repository, client, rootDir, fileUtils, gitCommandAuthAdapter, loginManager);
    });
  }

  public Observable<Boolean> hasRepositoryBeenCloned(@NonNull Repository repository) {
    return ObservableUtils.fromSynchronousCall(() -> {
      try {
        Git client = Git.open(getRepoRootDir(repository));
        return client.getRepository().getDirectory().exists();
      } catch (RepositoryNotFoundException e) {
        return false;
      }
    });
  }

  public boolean canPreV1RepoBeImported(@NonNull Repository repository) {
    File oldRootDir = getPreV1RootDir(repository);
    return oldRootDir.exists() && oldRootDir.listFiles().length > 0;
  }

  private File getRepoRootDir(@NonNull Repository repository) {
    return new File(context.getFilesDir(), PATH_REPOS_GITHUB + "/" + repository.getFullName());
  }

  private File getPreV1RootDir(@NonNull Repository repository) {
    return new File(context.getFilesDir(), repository.getFullName());
  }

  private void copyFiles(@NonNull File from, @NonNull File to) throws IOException {
    if (!from.isDirectory()) {
      Files.copy(from, to);
      return;
    }
    for (File file : from.listFiles()) {
      if (file.getName().equals(".git")) continue;
      copyFiles(file, new File(to, file.getName()));
    }
  }

}
