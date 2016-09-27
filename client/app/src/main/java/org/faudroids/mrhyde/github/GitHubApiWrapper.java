package org.faudroids.mrhyde.github;


import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.git.RepositoryOwner;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Simple wrapper class for converting the GitHuh client methods into an async version
 * by using RxJava.
 */
public final class GitHubApiWrapper {

	private final RepositoryService repositoryService;
	private final OrganizationService organizationService;

	@Inject
	public GitHubApiWrapper(
			RepositoryService repositoryService,
			OrganizationService organizationService) {

		this.repositoryService = repositoryService;
		this.organizationService = organizationService;
	}


	public Observable<List<Repository>> getRepositories() {
    return ObservableUtils
        .fromSynchronousCall(repositoryService::getRepositories)
        .flatMap(Observable::from)
        .map(Repository::fromGitHubRepository)
        .toList();
  }


	public Observable<List<Repository>> getOrgRepositories(final String orgName) {
    return ObservableUtils
        .fromSynchronousCall(() -> repositoryService.getOrgRepositories(orgName))
        .flatMap(Observable::from)
        .map(Repository::fromGitHubRepository)
        .toList();
  }


	public Observable<Repository> getRepository(final String ownerLogin, final String repoName) {
    return ObservableUtils
        .fromSynchronousCall(() -> repositoryService.getRepository(ownerLogin, repoName))
        .map(Repository::fromGitHubRepository);
	}


	public Observable<List<RepositoryOwner>> getOrganizations() {
    return ObservableUtils
        .fromSynchronousCall(organizationService::getOrganizations)
        .flatMap(Observable::from)
        .map(RepositoryOwner::fromGitHubUser)
        .toList();
	}

}
