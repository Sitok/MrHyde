package org.faudroids.mrhyde.gitlab;


import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

/**
 * Retrofit interface for fetching the access token.
 */
public interface GitLabAuthApi {

	@POST("/oauth/token")
  @FormUrlEncoded
  Observable<GitLabToken> getAccessToken(
      @Field("client_id") String clientId,
      @Field("client_secret") String clientSecret,
      @Field("grant_type") String grantType,
      @Field("redirect_uri") String redirectUri,
      @Field("code") String code
  );

  @POST("/oauth/token")
  @FormUrlEncoded
  Observable<GitLabToken> refreshAccessToken(
      @Field("grant_type") String grantType,
      @Field("refresh_token") String refreshToken
  );

}
