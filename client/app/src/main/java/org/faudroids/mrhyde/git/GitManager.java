package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.Git;
import org.faudroids.mrhyde.github.GitHubRepository;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.io.File;

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
    return ObservableUtils
        .fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
          fileUtils.deleteFile(rootDir);
          return null;
        });
  }

  public File getRootDir() {
    return rootDir;
  }

}
