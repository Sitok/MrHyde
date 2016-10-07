package org.faudroids.mrhyde.bitbucket;


import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

/**
 * Retrofit interface for fetching the access token.
 */
public interface BitbucketAuthApi {

	@POST("/site/oauth2/access_token")
  @FormUrlEncoded
  Observable<BitbucketToken> getAccessToken(
      @Field("grant_type") String grantType,
      @Field("code") String code
  );

  @POST("/site/oauth2/access_token")
  @FormUrlEncoded
  Observable<BitbucketToken> refreshAccessToken(
      @Field("grant_type") String grantType,
      @Field("refresh_token") String refreshToken
  );

}
