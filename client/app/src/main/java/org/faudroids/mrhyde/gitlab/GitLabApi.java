package org.faudroids.mrhyde.gitlab;

import android.content.Context;

import com.google.common.collect.Lists;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.git.RepositoryFactory;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class GitLabApi {

  private final Context context;
  private final GitLabAuthApi authApi;
  private final GitLabGeneralApi generalApi;
  private final RepositoryFactory repositoryFactory;

  @Inject
  GitLabApi(Context context, GitLabAuthApi authApi, GitLabGeneralApi generalApi, RepositoryFactory repositoryFactory) {
    this.context = context;
    this.authApi = authApi;
    this.generalApi = generalApi;
    this.repositoryFactory = repositoryFactory;
  }

  public Observable<GitLabToken> getAccessToken(String code) {
    return authApi.getAccessToken(
        context.getString(R.string.gitLabClientId),
        context.getString(R.string.gitLabClientSecret),
        "authorization_code",
        "http://localhost",
        code
    );
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
