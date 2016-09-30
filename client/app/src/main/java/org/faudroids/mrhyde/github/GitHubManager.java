package org.faudroids.mrhyde.github;


import org.faudroids.mrhyde.git.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Manages high level GitHub specific operations, such as fetching repositories,
 * OAuth, etc.
 */
@Singleton
public final class GitHubManager {

  private final GitHubApiWrapper gitHubApiWrapper;
  private Map<String, Repository> allRepositoryMap;

  @Inject
  GitHubManager(GitHubApiWrapper gitHubApiWrapper) {
    this.gitHubApiWrapper = gitHubApiWrapper;
  }

  public Observable<Collection<Repository>> getAllRepositories() {
    // cache?
    if (allRepositoryMap != null) {
      return Observable.just(allRepositoryMap.values());
    }

    // fetch
    return Observable.zip(
        gitHubApiWrapper.getRepositories(),
        gitHubApiWrapper.getOrganizations()
            .flatMap(Observable::from)
            .flatMap(org -> gitHubApiWrapper.getOrgRepositories(org.getUsername()))
            .toList(),
        (userRepos, orgRepos) -> {
          List<Repository> allRepos = new ArrayList<>(userRepos);
          for (List<Repository> repos : orgRepos) allRepos.addAll(repos);
          return allRepos;
        })
        .flatMap(repositories -> {
          allRepositoryMap = new HashMap<>();
          for (Repository repository : repositories) {
            allRepositoryMap.put(repository.getFullName(), repository);
          }
          return Observable.just(allRepositoryMap.values());
        });
  }

}
