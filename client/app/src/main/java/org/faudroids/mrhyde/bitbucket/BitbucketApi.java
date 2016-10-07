package org.faudroids.mrhyde.bitbucket;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.git.RepositoryFactory;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.net.URLDecoder;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class BitbucketApi {

  private final BitbucketAuthApi authApi;
  private final BitbucketGeneralApi generalApi;
  private final RepositoryFactory repositoryFactory;

  @Inject
  BitbucketApi(BitbucketAuthApi authApi, BitbucketGeneralApi generalApi, RepositoryFactory repositoryFactory) {
    this.authApi = authApi;
    this.generalApi = generalApi;
    this.repositoryFactory = repositoryFactory;
  }

  public Observable<BitbucketToken> getAccessToken(String code) {
    return authApi.getAccessToken("authorization_code", code);
  }

  public Observable<List<Repository>> getRepositories(String role) {
    // get all repository pages
    return ObservableUtils.fromSynchronousCall(() -> {
      List<Repository> repositories = Lists.newArrayList();

      String after = null;
      do {
        BitbucketRepositoriesPage page = generalApi.getRepositories(role, after).toBlocking().first();

        // read repositories
        for (BitbucketRepository repository : page.getValues()) {
          // skip non git repositories
          if (!repository.getScm().equals("git")) {
            continue;
          }
          repositories.add(repositoryFactory.fromBitbucketRepository(repository));
        }

        // check pagination
        String nextUrl = page.getNext();
        if (nextUrl != null) {
          String query = nextUrl.split("\\?")[1];
          after = Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query).get("after");
          if (after != null) after = URLDecoder.decode(after);
        } else {
          after = null;
        }

      } while (after != null);

      return repositories;
    });
  }

  public Observable<BitbucketUser> getUser() {
    return generalApi.getUser();
  }

  public Observable<Optional<String>> getUserPrimaryEmail() {
    return generalApi
        .getUserEmails()
        .map(bitbucketEmailsPage -> {
          for (BitbucketEmail email : bitbucketEmailsPage.getValues()) {
            if (email.getType().equals("email") && email.isPrimary()) {
              return Optional.of(email.getEmail());
            }
          }
          return Optional.absent();
        });
  }

}
