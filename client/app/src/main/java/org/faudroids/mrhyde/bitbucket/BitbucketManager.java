package org.faudroids.mrhyde.bitbucket;


import com.google.common.collect.Maps;

import org.faudroids.mrhyde.git.Repository;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Manages high level Bitbucket specific operations, such as fetching repositories,
 * OAuth, etc.
 */
@Singleton
public final class BitbucketManager {

  private final BitbucketApi bitbucketApi;
  private Map<String, Repository> allRepositoryMap;

  @Inject
  BitbucketManager(BitbucketApi bitbucketApi) {
    this.bitbucketApi = bitbucketApi;
  }

  public Observable<Collection<Repository>> getAllRepositories() {
    // cache?
    if (allRepositoryMap != null) {
      return Observable.just(allRepositoryMap.values());
    }

    return bitbucketApi
        .getRepositories("contributor")
        .map(repositories -> {
          allRepositoryMap = Maps.newHashMap();
          for (Repository repository : repositories) {
            allRepositoryMap.put(repository.getFullName(), repository);
          }
          return repositories;
        });
  }

}
