package org.faudroids.mrhyde.gitlab;

import org.faudroids.mrhyde.auth.LoginManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module
public class GitLabModule {

  @Provides
  @Singleton
  public GitLabGeneralApi provideGitLabGeneralApi(LoginManager loginManager) {
    return new RestAdapter.Builder()
        .setEndpoint("https://gitlab.com/api/v3/")
        .setRequestInterceptor(request -> {
          String token = loginManager.getGitLabAccount().getPersonalAccessToken();
          request.addHeader("PRIVATE-TOKEN", token);
        })
        .build()
        .create(GitLabGeneralApi.class);
  }

}
