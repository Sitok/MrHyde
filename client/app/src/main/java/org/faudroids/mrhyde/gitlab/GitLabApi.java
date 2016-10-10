package org.faudroids.mrhyde.gitlab;

import com.google.common.collect.Lists;

import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.git.RepositoryFactory;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class GitLabApi {

  private final GitLabGeneralApi generalApi;
  private final RepositoryFactory repositoryFactory;

  @Inject
  GitLabApi(GitLabGeneralApi generalApi, RepositoryFactory repositoryFactory) {
    this.generalApi = generalApi;
    this.repositoryFactory = repositoryFactory;
  }

  public Observable<List<Repository>> getRepositories() {
    return generalApi
        .getProjects()
        .map(gitLabProjects -> {
          List<Repository> repos = Lists.newArrayList();
          for (GitLabProject project : gitLabProjects) {
            repos.add(repositoryFactory.fromGitLabProject(project));
          }
          return repos;
        });
  }

  public Observable<GitLabUser> getUser() {
    return generalApi.getUser();
  }

}
