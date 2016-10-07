package org.faudroids.mrhyde.git;

import org.faudroids.mrhyde.auth.LoginManager;

import javax.inject.Inject;

/**
 * Factory pattern for creating instances of {@link GitCommandAuthAdapter}.
 */
public class GitCommandAuthAdapterFactory {

  private final LoginManager loginManager;

  @Inject
  GitCommandAuthAdapterFactory(LoginManager loginManager) {
    this.loginManager = loginManager;
  }

  public GitCommandAuthAdapter create(Repository repository) {
    return new GitCommandAuthAdapter(loginManager, repository);
  }

}

