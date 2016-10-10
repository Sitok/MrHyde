package org.faudroids.mrhyde.gitlab;


import java.util.List;

import retrofit.http.GET;
import rx.Observable;

/**
 * Retrofit interface for various GitLab endpoints that use the Oauth 2 access token
 * for authentication / authorization.
 */
public interface GitLabGeneralApi {

	@GET("/projects")
  Observable<List<GitLabProject>> getProjects();

  @GET("/user")
  Observable<GitLabUser> getUser();

}
