package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.auth.OAuthAccessTokenProvider;
import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.github.GitHubAccount;
import org.faudroids.mrhyde.gitlab.GitLabAccount;

import rx.Observable;

/**
 * Adds authentication to a {@link org.eclipse.jgit.api.TransportCommand}.
 */
public class GitCommandAuthAdapter {

  private final LoginManager loginManager;
  private final OAuthAccessTokenProvider accessTokenProvider;
  private final Repository repository;

  private final CredentialsProviderCreator credentialsProviderCreator = new CredentialsProviderCreator();

  GitCommandAuthAdapter(LoginManager loginManager, OAuthAccessTokenProvider accessTokenProvider, Repository repository) {
    this.loginManager = loginManager;
    this.accessTokenProvider = accessTokenProvider;
    this.repository = repository;
  }

  public <T extends TransportCommand<T, ?>> Observable<T> wrap(@NonNull T command) {
    Account account = loginManager.getAccount(repository);
    return account
        .accept(accessTokenProvider, null)
        .map(accessToken -> account.accept(credentialsProviderCreator, accessToken))
        .map(command::setCredentialsProvider);
  }

  private class CredentialsProviderCreator implements AccountVisitor<String, CredentialsProvider> {

    @Override
    public CredentialsProvider visit(GitHubAccount account, String accessToken) {
      return new UsernamePasswordCredentialsProvider(
          account.getLogin(),
          account.getAccessToken()
      );
    }

    @Override
    public CredentialsProvider visit(BitbucketAccount account, String accessToken) {
      return new UsernamePasswordCredentialsProvider(
          "x-token-auth",
          accessToken
      );
    }

    @Override
    public CredentialsProvider visit(GitLabAccount account, String accessToken) {
      return new UsernamePasswordCredentialsProvider(
          "gitlab-ci-token",
          accessToken
      );
    }

  }

}
