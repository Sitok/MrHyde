package org.faudroids.mrhyde.git;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.io.Files;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.faudroids.mrhyde.auth.LoginManager;
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
class GitManagerFactory {

  private final Context context;
  private final LoginManager loginManager;
  private final FileUtils fileUtils;
  private final GitCommandAuthAdapterFactory gitCommandAuthAdapterFactory;

  @Inject
  public GitManagerFactory(
      Context context,
      LoginManager loginManager,
      FileUtils fileUtils,
      GitCommandAuthAdapterFactory gitCommandAuthAdapterFactory) {
    this.context = context;
    this.loginManager = loginManager;
    this.fileUtils = fileUtils;
    this.gitCommandAuthAdapterFactory = gitCommandAuthAdapterFactory;
  }

  public GitManager openRepository(@NonNull final Repository repository) {
    try {
      Git client = Git.open(repository.getRootDir());
      GitCommandAuthAdapter authAdapter = gitCommandAuthAdapterFactory.create(repository);
      return new GitManager(repository, client, fileUtils, authAdapter, loginManager);
    } catch (IOException e) {
      Timber.e(e, "Failed to open local git repository");
      return null;
    }
  }

  public Observable<GitManager> cloneRepository(@NonNull Repository repository, boolean importPreV1Repo) {
    return ObservableUtils.fromSynchronousCall(() -> {
      GitCommandAuthAdapter authAdapter = gitCommandAuthAdapterFactory.create(repository);
      // clone repo
      Git client = authAdapter
          .wrap(Git
              .cloneRepository()
              .setURI(repository.getCloneUrl())
              .setDirectory(repository.getRootDir())
          )
          .toBlocking().first()
          .call();

      // copy v1 files
      File preV1RootDir = getPreV1RootDir(repository);
      if (importPreV1Repo) {
        copyFiles(preV1RootDir, repository.getRootDir());
      }

      // delete pre v1 files
      if (preV1RootDir.exists()) {
        fileUtils.deleteFile(preV1RootDir).toBlocking().first();
      }

      return new GitManager(repository, client, fileUtils, authAdapter, loginManager);
    });
  }

  public Observable<Boolean> hasRepositoryBeenCloned(@NonNull Repository repository) {
    return ObservableUtils.fromSynchronousCall(() -> {
      try {
        Git client = Git.open(repository.getRootDir());
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
