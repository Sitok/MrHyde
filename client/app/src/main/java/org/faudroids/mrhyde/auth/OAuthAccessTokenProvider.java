package org.faudroids.mrhyde.auth;

import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.bitbucket.BitbucketApi;
import org.faudroids.mrhyde.bitbucket.BitbucketToken;
import org.faudroids.mrhyde.github.GitHubAccount;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

/**
 * Creates if necessary an update to date OAuth2 access token
 * for a given {@link Account}.
 */
public class OAuthAccessTokenProvider implements AccountVisitor<Void, Observable<String>> {

  private final BitbucketApi bitbucketApi;

  @Inject
  OAuthAccessTokenProvider(BitbucketApi bitbucketApi) {
    this.bitbucketApi = bitbucketApi;
  }

  @Override
  public Observable<String> visit(GitHubAccount account, Void param) {
    return Observable.just(account.getAccessToken());
  }

  @Override
  public Observable<String> visit(BitbucketAccount account, Void param) {
    // fetch new access token before every (!) bitbucket request. Access tokens expire within
    // 60 minutes (optimize?).
    Timber.d("Refreshing bitbucket access token");
    return bitbucketApi
        .refreshToken(account.getRefreshToken())
        .map(BitbucketToken::getAccessToken);
  }
}
