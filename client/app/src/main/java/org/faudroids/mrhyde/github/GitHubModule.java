package org.faudroids.mrhyde.github;


import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;

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
  public GitHubEmailsApi provideEmailsApi() {
    return new RestAdapter.Builder()
        .setEndpoint("https://api.github.com")
        .build()
        .create(GitHubEmailsApi.class);
  }

  @Singleton
  @Provides
  public RepositoryService provideRepositoryService() {
    return new RepositoryService();
  }


  @Singleton
  @Provides
  public OrganizationService provideOrganizationService() {
    return new OrganizationService();
  }

}
