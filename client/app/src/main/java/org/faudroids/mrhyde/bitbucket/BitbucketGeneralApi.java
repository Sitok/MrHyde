package org.faudroids.mrhyde.bitbucket;


import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Retrofit interface for various Bitbucket endpoints that use the Oauth 2 access token
 * for authentication / authorization.
 */
public interface BitbucketGeneralApi {

	@GET("/repositories")
  Observable<BitbucketRepositoriesPage> getRepositories(@Query("role") String role, @Query("after") String after);

}
