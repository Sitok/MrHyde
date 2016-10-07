package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.snappydb.DB;

import org.faudroids.mrhyde.utils.ObservableUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Keeps track of cloned repositories. Uses a SnappyDB instance for storing repo
 * meta data.
 */
@Singleton
public class RepositoriesManager {

  private final String KEY_REPOS = "repos";

  private final DB repositoriesDb;
  private final GitManagerFactory gitManagerFactory;

  private List<Repository> repositoriesCache = null;

  @Inject
  RepositoriesManager(
      @Named(GitModule.DB_REPOSITORIES_NAME) DB repositoriesDb,
      GitManagerFactory gitManagerFactory) {

    this.repositoriesDb = repositoriesDb;
    this.gitManagerFactory = gitManagerFactory;
  }

  /**
   * @return all clone repositories that are on this device.
   */
  public Observable<Collection<Repository>> getClonedRepositories() {
    // cache?
    if (repositoriesCache != null) {
      return Observable.just(Lists.newArrayList(repositoriesCache));
    }

    // load from DB
    return ObservableUtils
        .fromSynchronousCall(() -> {
          if (repositoriesDb.exists(KEY_REPOS))  {
            return repositoriesDb.getObjectArray(KEY_REPOS, Repository.class);
          }
          return new Repository[0];
        })
        .map(repositories -> {
          repositoriesCache = Lists.newArrayList(Arrays.asList(repositories));
          return repositoriesCache;
        });
  }

  /**
   * Opens a clone repository (= create an instance of {@link GitManager}).
   */
  public GitManager openRepository(@NonNull final Repository repository) {
    return gitManagerFactory.openRepository(repository);
  }

  /**
   * Clone a remote repository to this device.
   */
  public Observable<GitManager> cloneRepository(
      @NonNull Repository repository,
      boolean importPreV1Repo) {

    return gitManagerFactory
        .cloneRepository(repository, importPreV1Repo)
        .flatMap(gitManager -> ObservableUtils.fromSynchronousCall(() -> {
          // store repo in Db
          repositoriesCache.add(repository);
          repositoriesDb.put(KEY_REPOS, repositoriesCache.toArray());
          return gitManager;
        }));
  }

  /**
   * Delete a cloned repository from this device.
   */
  public Observable<Void> deleteRepository(@NonNull GitManager gitManager) {
    return gitManager
        .deleteAllLocalContent()
        .flatMap(aVoid -> ObservableUtils.fromSynchronousCall(() -> {
          repositoriesCache.remove(gitManager.getRepository());
          repositoriesDb.put(KEY_REPOS, repositoriesCache.toArray());
          return null;
        }));
  }

  /**
   * Sets the favorite field of a repository.
   * @return the updated repository.
   */
  public Observable<Repository> setRepositoryFavoriteStatus(Repository repository, boolean isFavorite) {
    Repository updatedRepo = new Repository(
        repository.getName(),
        repository.getCloneUrl(),
        isFavorite,
        repository.getAuthType(),
        repository.getHostingProvider(),
        repository.getRootDir(),
        repository.getOwner()
    );
    repositoriesCache.remove(repository);
    repositoriesCache.add(updatedRepo);
    return ObservableUtils.fromSynchronousCall(() -> {
      repositoriesDb.put(KEY_REPOS, repositoriesCache.toArray());
      return updatedRepo;
    });
  }

  /**
   * Checks if a pre v1 repository can be imported.
   */
  public boolean canPreV1RepoBeImported(@NonNull Repository repository) {
    return gitManagerFactory.canPreV1RepoBeImported(repository);
  }

}
