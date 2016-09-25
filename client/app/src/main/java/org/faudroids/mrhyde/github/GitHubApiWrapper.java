package org.faudroids.mrhyde.github;


import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.exceptions.OnErrorThrowable;

/**
 * Simple wrapper class for converting the GitHuh client methods into an async version
 * by using RxJava.
 *
 * Methods are added as needed.
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


	public Observable<List<GitHubRepository>> getRepositories() {
		return new Wrapper<List<Repository>>() {
			@Override
			protected List<Repository> doWrapMethod() throws Exception {
				return repositoryService.getRepositories();
      }
    }
        .wrapMethod()
        .flatMap(Observable::from)
        .map(GitHubRepository::new)
        .toList();
  }


	public Observable<List<GitHubRepository>> getOrgRepositories(final String orgName) {
		return new Wrapper<List<Repository>>() {
			@Override
			protected List<Repository> doWrapMethod() throws Exception {
				return repositoryService.getOrgRepositories(orgName);
			}
    }
        .wrapMethod()
        .flatMap(Observable::from)
        .map(GitHubRepository::new)
        .toList();
  }


	public Observable<GitHubRepository> getRepository(final String ownerLogin, final String repoName) {
		return new Wrapper<Repository>() {
			@Override
			protected Repository doWrapMethod() throws Exception {
				return repositoryService.getRepository(ownerLogin, repoName);
			}
		}
        .wrapMethod()
        .map(GitHubRepository::new);
	}


	public Observable<List<GitHubUser>> getOrganizations() {
		return new Wrapper<List<User>>() {
			@Override
			protected List<User> doWrapMethod() throws Exception {
				return organizationService.getOrganizations();
			}
		}
        .wrapMethod()
        .flatMap(Observable::from)
        .map(GitHubUser::new)
        .toList();
	}


	private static abstract class Wrapper<T> {

		public Observable<T> wrapMethod() {
			return Observable.defer(() -> {
        try {
          return Observable.just(doWrapMethod());
        } catch (Exception e) {
          throw OnErrorThrowable.from(e);
        }

      });
		}

		protected abstract T doWrapMethod() throws Exception;

	}

}
