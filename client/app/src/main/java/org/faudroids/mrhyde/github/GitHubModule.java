package org.faudroids.mrhyde.github;


import org.eclipse.egit.github.core.service.GitHubService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.faudroids.mrhyde.auth.LoginManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module
public class GitHubModule {

  @Singleton
  @Provides
  public GitHubAuthApi provideAuthApi() {
    return new RestAdapter.Builder()
        .setEndpoint("https://github.com")
        .build()
        .create(GitHubAuthApi.class);
  }

  @Singleton
  @Provides
  public GitHubEmailsApi provideEmailsApi(final LoginManager loginManager) {
    return new RestAdapter.Builder()
        .setEndpoint("https://api.github.com")
        .build()
        .create(GitHubEmailsApi.class);
  }

  @Singleton
  @Provides
  public RepositoryService provideRepositoryService(LoginManager loginManager) {
    return setAuthToken(new RepositoryService(), loginManager);
  }


  @Singleton
  @Provides
  public OrganizationService provideOrganizationService(LoginManager loginManager) {
    return setAuthToken(new OrganizationService(), loginManager);
  }


  private <T extends GitHubService> T setAuthToken(T service, LoginManager loginManager) {
    service.getClient().setOAuth2Token(loginManager.getAccount().getAccessToken());
    return service;
  }

}
