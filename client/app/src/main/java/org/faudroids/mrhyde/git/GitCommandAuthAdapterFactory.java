package org.faudroids.mrhyde.git;

import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.auth.OAuthAccessTokenProvider;

import javax.inject.Inject;

/**
 * Factory pattern for creating instances of {@link GitCommandAuthAdapter}.
 */
public class GitCommandAuthAdapterFactory {

  private final LoginManager loginManager;
  private final OAuthAccessTokenProvider accessTokenProvider;

  @Inject
  GitCommandAuthAdapterFactory(LoginManager loginManager, OAuthAccessTokenProvider accessTokenProvider) {
    this.loginManager = loginManager;
    this.accessTokenProvider = accessTokenProvider;
  }

  public GitCommandAuthAdapter create(Repository repository) {
    return new GitCommandAuthAdapter(loginManager, accessTokenProvider, repository);
  }

}

