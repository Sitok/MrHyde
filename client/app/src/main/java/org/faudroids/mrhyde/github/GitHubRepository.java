package org.faudroids.mrhyde.github;

import android.support.annotation.NonNull;

import org.eclipse.egit.github.core.Repository;

import java.io.Serializable;
import java.util.Date;

/**
 * A single repository hosted on GitHub.com.
 */
public class GitHubRepository implements Serializable {

  private final Repository repository;
  private final GitHubUser owner;

  public GitHubRepository(@NonNull  Repository repository) {
    this.repository = repository;
    this.owner = new GitHubUser(repository.getOwner());
  }

  public long getId() {
    return repository.getId();
  }

  public String getName() {
    return repository.getName();
  }

  public String getCloneUrl() {
    return repository.getCloneUrl();
  }

  public Date getPushedAt() {
    return repository.getPushedAt();
  }

  public String getFullName() {
    return String.format("%s/%s", owner.getLogin(), repository.getName());
  }

  public String getDefaultBranch() {
    return repository.getDefaultBranch();
  }

  public GitHubUser getOwner() {
    return owner;
  }

  public Repository getRepository() {
    return repository;
  }

}
