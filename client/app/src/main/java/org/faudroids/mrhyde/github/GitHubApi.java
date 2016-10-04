package org.faudroids.mrhyde.github;


import android.content.Context;

import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.git.RepositoryFactory;
import org.faudroids.mrhyde.git.RepositoryOwner;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Facade pattern for accessing all relevant GitHub API endpoints.
 */
public final class GitHubApi {

  private final Context context;
  private final GitHubAuthApi authApi;
  private final GitHubEmailsApi emailsApi;
  private final RepositoryService repositoryService;
  private final OrganizationService organizationService;
  private final RepositoryFactory repositoryFactory;

  @Inject
  public GitHubApi(
      Context context,
      GitHubAuthApi authApi,
      GitHubEmailsApi emailsApi,
      RepositoryService repositoryService,
      OrganizationService organizationService,
      RepositoryFactory repositoryFactory) {

    this.context = context;
    this.authApi = authApi;
    this.emailsApi = emailsApi;
    this.repositoryService = repositoryService;
    this.organizationService = organizationService;
    this.repositoryFactory = repositoryFactory;
  }

  public Observable<GitHubToken> getAccessToken(String code) {
    return authApi.getAccessToken(
        context.getString(R.string.gitHubClientId),
        context.getString(R.string.gitHubClientSecret),
        code
    );
  }

  public Observable<List<GitHubEmail>> getEmails(String token) {
    return emailsApi.getEmails(token);
  }

	public Observable<List<Repository>> getRepositories() {
    return ObservableUtils
        .fromSynchronousCall(repositoryService::getRepositories)
        .flatMap(Observable::from)
        .map(repositoryFactory::fromGitHubRepository)
        .toList();
  }

	public Observable<List<Repository>> getOrgRepositories(final String orgName) {
    return ObservableUtils
        .fromSynchronousCall(() -> repositoryService.getOrgRepositories(orgName))
        .flatMap(Observable::from)
        .map(repositoryFactory::fromGitHubRepository)
        .toList();
  }

	public Observable<Repository> getRepository(final String ownerLogin, final String repoName) {
    return ObservableUtils
        .fromSynchronousCall(() -> repositoryService.getRepository(ownerLogin, repoName))
        .map(repositoryFactory::fromGitHubRepository);
	}

	public Observable<List<RepositoryOwner>> getOrganizations() {
    return ObservableUtils
        .fromSynchronousCall(organizationService::getOrganizations)
        .flatMap(Observable::from)
        .map(repositoryFactory::fromGitHubUser)
        .toList();
	}

}
