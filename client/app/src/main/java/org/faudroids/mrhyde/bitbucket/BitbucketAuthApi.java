package org.faudroids.mrhyde.bitbucket;


import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

/**
 * Retrofit interface for fetching the access token.
 */
public interface BitbucketAuthApi {

	@POST("/site/oauth2/access_token?grant_type=authorization")
  @FormUrlEncoded
  Observable<BitbucketToken> getAccessToken(
      @Field("grant_type") String grantType,
      @Field("code") String code
  );

}
