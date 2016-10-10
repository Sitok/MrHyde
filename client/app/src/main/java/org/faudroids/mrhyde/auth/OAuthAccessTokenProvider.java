package org.faudroids.mrhyde.auth;

import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.bitbucket.BitbucketAuthApi;
import org.faudroids.mrhyde.bitbucket.BitbucketToken;
import org.faudroids.mrhyde.github.GitHubAccount;
import org.faudroids.mrhyde.gitlab.GitLabAccount;

import javax.inject.Inject;

import rx.Observable;

/**
 * Creates if necessary an update to date OAuth2 access token
 * for a given {@link Account}.
 */
public class OAuthAccessTokenProvider implements AccountVisitor<Void, Observable<String>> {

  private final BitbucketAuthApi bitbucketAuthApi;

  @Inject
  OAuthAccessTokenProvider(BitbucketAuthApi authApi) {
    this.bitbucketAuthApi = authApi;
  }

  @Override
  public Observable<String> visit(GitHubAccount account, Void param) {
    return Observable.just(account.getAccessToken());
  }

  @Override
  public Observable<String> visit(BitbucketAccount account, Void param) {
    // fetch new access token before every (!) bitbucket request. Access tokens expire within
    // 60 minutes (optimize?).
    return bitbucketAuthApi
        .refreshAccessToken("refresh_token", account.getRefreshToken())
        .map(BitbucketToken::getAccessToken);
  }

  @Override
  public Observable<String> visit(GitLabAccount account, Void param) {
    // not supported!
    return Observable.just(null);
  }
}
