package org.faudroids.mrhyde.git;

import android.content.Context;

import com.google.common.base.Optional;

import org.eclipse.egit.github.core.User;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory pattern for creating {@link Repository} instances.
 */
@Singleton
public class RepositoryFactory {

  private static final String PATH_REPOS_GITHUB = "github";

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
        AuthType.GITHUB_API_TOKEN,
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

}
