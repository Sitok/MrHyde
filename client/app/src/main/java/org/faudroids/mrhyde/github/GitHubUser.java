package org.faudroids.mrhyde.github;

import android.support.annotation.NonNull;

import org.eclipse.egit.github.core.User;

import java.io.Serializable;

/**
 * A single user / organization present on GitHub.
 */
public class GitHubUser implements Serializable {

  private final User user;

  public GitHubUser(@NonNull  User user) {
    this.user = user;
  }

  public String getLogin() {
    return user.getLogin();
  }

  public String getAvatarUrl() {
    return user.getAvatarUrl();
  }

}
