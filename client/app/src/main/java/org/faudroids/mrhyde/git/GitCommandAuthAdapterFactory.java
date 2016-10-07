package org.faudroids.mrhyde.git;

import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.bitbucket.BitbucketApi;

import javax.inject.Inject;

/**
 * Factory pattern for creating instances of {@link GitCommandAuthAdapter}.
 */
public class GitCommandAuthAdapterFactory {

  private final LoginManager loginManager;
  private final BitbucketApi bitbucketApi;

  @Inject
  GitCommandAuthAdapterFactory(LoginManager loginManager, BitbucketApi bitbucketApi) {
    this.loginManager = loginManager;
    this.bitbucketApi = bitbucketApi;
  }

  public GitCommandAuthAdapter create(Repository repository) {
    return new GitCommandAuthAdapter(loginManager, bitbucketApi, repository);
  }

}

