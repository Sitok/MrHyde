package org.faudroids.mrhyde.github;


import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.Collection;
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
	private final CommitService commitService;
	private final DataService dataService;
	private final OrganizationService organizationService;

	@Inject
	public GitHubApiWrapper(
			RepositoryService repositoryService,
			CommitService commitService,
			DataService dataService,
			OrganizationService organizationService) {

		this.repositoryService = repositoryService;
		this.commitService = commitService;
		this.dataService = dataService;
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


	public Observable<List<RepositoryCommit>> getCommits(final GitHubRepository repository) {
		return new Wrapper<List<RepositoryCommit>>() {
			@Override
			protected List<RepositoryCommit> doWrapMethod() throws Exception {
				return commitService.getCommits(repository.getRepository());
			}
		}.wrapMethod();
	}


	public Observable<Commit> createCommit(final GitHubRepository repository, final Commit commit) {
		return new Wrapper<Commit>() {
			@Override
			protected Commit doWrapMethod() throws Exception {
				return dataService.createCommit(repository.getRepository(), commit);
			}
		}.wrapMethod();
	}


	public Observable<Tree> getTree(final GitHubRepository repository, final String sha, final boolean recursive) {
		return new Wrapper<Tree>() {
			@Override
			protected Tree doWrapMethod() throws Exception {
				return dataService.getTree(repository.getRepository(), sha, recursive);
			}
		}.wrapMethod();
	}


	public Observable<Tree> createTree(final GitHubRepository repository, final Collection<TreeEntry> entries) {
		return new Wrapper<Tree>() {
			@Override
			protected Tree doWrapMethod() throws Exception {
				return dataService.createTree(repository.getRepository(), entries);
			}
		}.wrapMethod();
	}


	public Observable<Blob> getBlob(final GitHubRepository repository, final String sha) {
		return new Wrapper<Blob>() {
			@Override
			protected Blob doWrapMethod() throws Exception {
				return dataService.getBlob(repository.getRepository(), sha);
			}
		}.wrapMethod();
	}


	public Observable<String> createBlob(final GitHubRepository repository, final Blob blob) {
		return new Wrapper<String>() {
			@Override
			protected String doWrapMethod() throws Exception {
				return dataService.createBlob(repository.getRepository(), blob);
			}
		}.wrapMethod();
	}


	public Observable<Reference> getReference(final GitHubRepository repository, final String ref) {
		return new Wrapper<Reference>() {
			@Override
			protected Reference doWrapMethod() throws Exception {
				return dataService.getReference(repository.getRepository(), ref);
			}
		}.wrapMethod();
	}


	public Observable<Reference> editReference(final GitHubRepository repository, final Reference reference) {
		return new Wrapper<Reference>() {
			@Override
			protected Reference doWrapMethod() throws Exception {
				return dataService.editReference(repository.getRepository(), reference);
			}
		}.wrapMethod();
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
