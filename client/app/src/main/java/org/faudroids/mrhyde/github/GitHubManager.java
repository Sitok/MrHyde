package org.faudroids.mrhyde.github;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.RetrofitError;
import rx.Observable;
import timber.log.Timber;

/**
 * Manages high level GitHub specific operations, such as fetching repositories,
 * OAuth, etc.
 */
@Singleton
public final class GitHubManager {

  private static final String PREFS_NAME = GitHubManager.class.getName();

  private final Context context;
  private final GitHubApiWrapper gitHubApiWrapper;
  private Map<String, GitHubRepository> allRepositoryMap, favouriteRepositoriesMap;


  @Inject
  GitHubManager(Context context, GitHubApiWrapper gitHubApiWrapper) {
    this.context = context;
    this.gitHubApiWrapper = gitHubApiWrapper;
  }


  public Observable<Collection<GitHubRepository>> getAllRepositories() {
    // cache?
    if (allRepositoryMap != null) {
      return Observable.just(allRepositoryMap.values());
    }

    // fetch
    return Observable.zip(
        gitHubApiWrapper.getRepositories(),
        gitHubApiWrapper.getOrganizations()
            .flatMap(Observable::from)
            .flatMap(org -> gitHubApiWrapper.getOrgRepositories(org.getLogin()))
            .toList(),
        (userRepos, orgRepos) -> {
          List<GitHubRepository> allRepos = new ArrayList<>(userRepos);
          for (List<GitHubRepository> repos : orgRepos) allRepos.addAll(repos);
          return allRepos;
        })
        .flatMap(repositories -> {
          allRepositoryMap = new HashMap<>();
          for (GitHubRepository repository : repositories) {
            allRepositoryMap.put(repository.getFullName(), repository);
          }
          return Observable.just(allRepositoryMap.values());
        });
  }


  public boolean hasFavouriteRepositories() {
    if (favouriteRepositoriesMap != null) return !favouriteRepositoriesMap.isEmpty();
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    return !prefs.getAll().isEmpty();
  }


  public Observable<Collection<GitHubRepository>> getFavouriteRepositories() {
    // get cached values
    if (favouriteRepositoriesMap != null) {
      return Observable.just(favouriteRepositoriesMap.values());
    }

    // get favourite repos from all cached
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    Set<String> repoNames = prefs.getAll().keySet();

    if (allRepositoryMap != null) {
      favouriteRepositoriesMap = new HashMap<>();
      for (String repoName : repoNames) {
        GitHubRepository repo = allRepositoryMap.get(repoName);
        if (repo == null) {
          Timber.w("failed to find favourite repo " + repoName);
          unmarkRepositoryAsFavourite(repoName);
          continue;
        }
        favouriteRepositoriesMap.put(repoName, allRepositoryMap.get(repoName));
      }
      return Observable.just(favouriteRepositoriesMap.values());
    }

    // download favourite repos
    return Observable.from(repoNames)
        .flatMap(repoName -> {
          String[] repoParts = repoName.split("/");
          return gitHubApiWrapper.getRepository(repoParts[0], repoParts[1]);
        })
        .onErrorResumeNext(throwable -> {
          // ignore 404's, as repo might no longer exist
          if (throwable instanceof RetrofitError) {
            RetrofitError error = (RetrofitError) throwable;
            if (error.getResponse() != null && error.getResponse().getStatus() == 404) {
              return null;
            }
          }
          return Observable.error(throwable);
        })
        .toList()
        .flatMap(repositories -> {
          favouriteRepositoriesMap = new HashMap<>();
          for (GitHubRepository repo : repositories) {
            favouriteRepositoriesMap.put(repo.getFullName(), repo);
          }
          return Observable.<Collection<GitHubRepository>>just(repositories);
        });
  }


  public void markRepositoryAsFavourite(GitHubRepository repository) {
    Timber.d("marking repo %s as favourite", repository.getFullName());
    SharedPreferences.Editor editor
        = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
    editor.putString(repository.getFullName(), "");
    editor.apply();

    if (favouriteRepositoriesMap != null) {
      favouriteRepositoriesMap.put(repository.getFullName(), repository);
    }
  }


  public boolean isRepositoryFavourite(GitHubRepository repository) {
    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    return prefs.contains(repository.getFullName());
  }


  public void unmarkRepositoryAsFavourite(GitHubRepository repository) {
    unmarkRepositoryAsFavourite(repository.getFullName());
  }


  private void unmarkRepositoryAsFavourite(String repoName) {
    Timber.d("unmarking repo %s as favourite", repoName);
    SharedPreferences.Editor editor
        = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
    editor.remove(repoName);
    editor.apply();

    if (favouriteRepositoriesMap != null) {
      favouriteRepositoriesMap.remove(repoName);
    }
  }

}
