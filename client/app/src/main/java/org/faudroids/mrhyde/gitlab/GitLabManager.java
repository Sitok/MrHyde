package org.faudroids.mrhyde.gitlab;


import com.google.common.collect.Lists;

import org.faudroids.mrhyde.git.Repository;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Manages high level GitLab specific operations, such as fetching repositories,
 * OAuth, etc.
 */
@Singleton
public final class GitLabManager {

  private final GitLabApi gitLabApi;
  private List<Repository> allRepositories;

  @Inject
  GitLabManager(GitLabApi gitLabApi) {
    this.gitLabApi = gitLabApi;
  }

  public Observable<Collection<Repository>> getAllRepositories() {
    // cache?
    if (allRepositories != null) {
      return Observable.just(allRepositories);
    }

    return gitLabApi
        .getRepositories()
        .map(repositories -> {
          allRepositories = Lists.newArrayList();
          allRepositories.addAll(repositories);
          return repositories;
        });
  }

}
