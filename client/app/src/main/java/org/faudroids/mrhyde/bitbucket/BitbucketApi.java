package org.faudroids.mrhyde.bitbucket;

import javax.inject.Inject;

import rx.Observable;

public class BitbucketApi {

  private final BitbucketAuthApi authApi;
  private final BitbucketGeneralApi generalApi;

  @Inject
  BitbucketApi(BitbucketAuthApi authApi) {
    this.authApi = authApi;
  }

  public Observable<BitbucketToken> getAccessToken(String code) {
    return authApi.getAccessToken("authorization_code", code);
  }
}
