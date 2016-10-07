package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.github.GitHubAccount;

/**
 * Adds authentication to a {@link org.eclipse.jgit.api.TransportCommand}.
 */
public class GitCommandAuthAdapter {

  private final LoginManager loginManager;
  private final Repository repository;

  private final CredentialsProviderCreator credentialsProviderCreator = new CredentialsProviderCreator();

  GitCommandAuthAdapter(LoginManager loginManager, Repository repository) {
    this.loginManager = loginManager;
    this.repository = repository;
  }

  public <T extends TransportCommand<T, ?>> T wrap(@NonNull T command) {
    Account account = loginManager.getAccount(repository);
    return command.setCredentialsProvider(account.accept(credentialsProviderCreator, null));
  }

  private static class CredentialsProviderCreator implements AccountVisitor<Void, CredentialsProvider> {

    @Override
    public CredentialsProvider visit(GitHubAccount account, Void param) {
      return new UsernamePasswordCredentialsProvider(
          account.getLogin(),
          account.getAccessToken()
      );
    }

    @Override
    public CredentialsProvider visit(BitbucketAccount account, Void param) {
      return new UsernamePasswordCredentialsProvider(
          "x-token-auth",
          account.getAccessToken()
      );
    }
  }

}
