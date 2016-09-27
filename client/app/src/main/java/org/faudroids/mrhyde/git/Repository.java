package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import java.io.Serializable;

/**
 * A single locally available repository.
 */
public class Repository implements Serializable {

  protected final String name;
  protected final String cloneUrl;
  protected final Optional<RepositoryOwner> owner;


  public Repository(
      @NonNull String name,
      @NonNull String cloneUrl,
      @NonNull Optional<RepositoryOwner> owner) {
    this.name = name;
    this.cloneUrl = cloneUrl;
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public String getCloneUrl() {
    return cloneUrl;
  }

  public Optional<RepositoryOwner> getOwner() {
    return owner;
  }

  public String getFullName() {
    if (!owner.isPresent()) {
      return name;
    }
    return String.format("%s/%s", owner.get().getUsername(), name);
  }


  public static Repository fromGitHubRepository(org.eclipse.egit.github.core.Repository gitHubRepo) {
    return new Repository(
        gitHubRepo.getName(),
        gitHubRepo.getCloneUrl(),
        Optional.of(RepositoryOwner.fromGitHubUser(gitHubRepo.getOwner()))
    );
  }

}
