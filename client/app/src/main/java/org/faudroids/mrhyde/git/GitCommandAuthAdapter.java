package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.bitbucket.BitbucketApi;
import org.faudroids.mrhyde.github.GitHubAccount;

import rx.Observable;
import timber.log.Timber;

/**
 * Adds authentication to a {@link org.eclipse.jgit.api.TransportCommand}.
 */
public class GitCommandAuthAdapter {

  private final LoginManager loginManager;
  private final BitbucketApi bitbucketApi;
  private final Repository repository;

  private final CredentialsProviderCreator credentialsProviderCreator = new CredentialsProviderCreator();

  GitCommandAuthAdapter(LoginManager loginManager, BitbucketApi bitbucketApi, Repository repository) {
    this.loginManager = loginManager;
    this.bitbucketApi = bitbucketApi;
    this.repository = repository;
  }

  public <T extends TransportCommand<T, ?>> Observable<T> wrap(@NonNull T command) {
    Account account = loginManager.getAccount(repository);
    return account
        .accept(credentialsProviderCreator, null)
        .map(command::setCredentialsProvider);
  }

  private class CredentialsProviderCreator implements AccountVisitor<Void, Observable<CredentialsProvider>> {

    @Override
    public Observable<CredentialsProvider> visit(GitHubAccount account, Void param) {
      return Observable.just(new UsernamePasswordCredentialsProvider(
          account.getLogin(),
          account.getAccessToken()
      ));
    }

    @Override
    public Observable<CredentialsProvider> visit(BitbucketAccount account, Void param) {
      // fetch new access token before every (!) bitbucket request. Access tokens expire within
      // 60 minutes (optimize?).
      Timber.d("Refreshing bitbucket access token");
      return bitbucketApi
          .refreshToken(account.getRefreshToken())
          .map(token -> new UsernamePasswordCredentialsProvider(
              "x-token-auth",
              token.getAccessToken()
          ));
    }
  }

}
