package org.faudroids.mrhyde.gitlab;

import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.auth.OAuthAccessTokenProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module
public class GitLabModule {

  @Provides
  @Singleton
  public GitLabAuthApi provideGitLabAuthApi() {
    return new RestAdapter.Builder()
        .setEndpoint("https://gitlab.com")
        .build()
        .create(GitLabAuthApi.class);
  }

  @Provides
  @Singleton
  public GitLabGeneralApi provideGitLabGeneralApi(
      LoginManager loginManager,
      OAuthAccessTokenProvider accessTokenProvider) {
    return new RestAdapter.Builder()
        .setEndpoint("https://gitlab.com/api/v3/")
        .setRequestInterceptor(request -> {
          String accessToken = accessTokenProvider
              .visit(loginManager.getGitLabAccount(), null)
              .toBlocking()
              .first();
          request.addHeader("Authorization", "Bearer " + accessToken);
        })
        .build()
        .create(GitLabGeneralApi.class);
  }

}
