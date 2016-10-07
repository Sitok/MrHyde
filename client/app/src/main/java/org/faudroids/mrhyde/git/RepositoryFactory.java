package org.faudroids.mrhyde.git;

import android.content.Context;

import com.google.common.base.Optional;

import org.eclipse.egit.github.core.User;
import org.faudroids.mrhyde.bitbucket.BitbucketLink;
import org.faudroids.mrhyde.bitbucket.BitbucketRepository;
import org.faudroids.mrhyde.bitbucket.BitbucketUser;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory pattern for creating {@link Repository} instances.
 */
@Singleton
public class RepositoryFactory {

  private static final String PATH_REPOS_GITHUB = "github";
  private static final String PATH_REPOS_BITBUCKET = "bitbucket";

  private final Context context;

  @Inject
  RepositoryFactory(Context context) {
    this.context = context;
  }

  public Repository fromGitHubRepository(org.eclipse.egit.github.core.Repository gitHubRepo) {
    String name = gitHubRepo.getName();
    RepositoryOwner owner = fromGitHubUser(gitHubRepo.getOwner());
    File rootDir = new File(
        context.getFilesDir(),
        String.format("%s/%s/%s", PATH_REPOS_GITHUB, owner.getUsername(), name)
    );

    return new Repository(
        name,
        gitHubRepo.getCloneUrl(),
        false,
        AuthType.GITHUB_OAUTH2_ACCESS_TOKEN,
        rootDir,
        Optional.of(owner)
    );
  }

  public RepositoryOwner fromGitHubUser(User gitHubUser) {
    return new RepositoryOwner(
        gitHubUser.getLogin(),
        Optional.fromNullable(gitHubUser.getAvatarUrl())
    );
  }

  public Repository fromBitbucketRepository(BitbucketRepository bitbucketRepo) {
    String name = bitbucketRepo.getName();
    RepositoryOwner owner = fromBitbucketUser(bitbucketRepo.getOwner());
    File rootDir = new File(
        context.getFilesDir(),
        String.format("%s/%s/%s", PATH_REPOS_BITBUCKET, owner.getUsername(), name)
    );

    String cloneUrl = null;
    for (BitbucketLink link : bitbucketRepo.getLinks().getClone()) {
      if (link.getName().equals("https")) {
        cloneUrl = link.getHref();
      }
    }
    if (cloneUrl == null) throw new IllegalStateException("No clone url for " + bitbucketRepo.getName());

    return new Repository(
        name,
        cloneUrl,
        false,
        AuthType.BITBUCKET_OAUTH2_ACCESS_TOKEN,
        rootDir,
        Optional.of(owner)
    );
  }

  public RepositoryOwner fromBitbucketUser(BitbucketUser bitbucketUser) {
    String avatarUrl = bitbucketUser.getLinks().getAvatar() != null
        ? null
        : bitbucketUser.getLinks().getAvatar().getHref();
    bitbucketUser.getLinks().getAvatar();
    return new RepositoryOwner(
        bitbucketUser.getUsername(),
        Optional.fromNullable(avatarUrl)
    );
  }
}
