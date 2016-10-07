package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.faudroids.mrhyde.auth.LoginManager;

/**
 * Adds authentication to a {@link org.eclipse.jgit.api.TransportCommand}.
 */
public class GitCommandAuthAdapter {

  private final LoginManager loginManager;
  private final Repository repository;

  GitCommandAuthAdapter(LoginManager loginManager, Repository repository) {
    this.loginManager = loginManager;
    this.repository = repository;
  }

  public <T extends TransportCommand<T, ?>> T wrap(@NonNull T command) {
    CredentialsProvider credentialsProvider;
    switch (repository.getAuthType()) {
      case GITHUB_OAUTH2_ACCESS_TOKEN:
        credentialsProvider = new UsernamePasswordCredentialsProvider(
            loginManager.getGitHubAccount().getLogin(),
            loginManager.getGitHubAccount().getAccessToken()
        );
        break;
      case BITBUCKET_OAUTH2_ACCESS_TOKEN:
        credentialsProvider = new UsernamePasswordCredentialsProvider(
            "x-token-auth",
            loginManager.getBitbucketAccount().getAccessToken()
        );
        break;
      default:
        throw new IllegalStateException("Unsupported auth type " + repository.getAuthType());
    }

    return command.setCredentialsProvider(credentialsProvider);
  }

}
