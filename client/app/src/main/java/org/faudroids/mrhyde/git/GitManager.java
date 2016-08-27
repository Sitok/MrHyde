package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.Git;
import org.faudroids.mrhyde.github.GitHubRepository;

/**
 * Handles "low level" git operations for a single repository.
 */
public class GitManager {

  private final GitHubRepository repository;
  private final Git gitClient;

  public GitManager(@NonNull GitHubRepository repository, @NonNull Git gitClient) {
    this.repository = repository;
    this.gitClient = gitClient;
  }

  public void beNice() {
    System.out.println("Hello world");
  }

}
