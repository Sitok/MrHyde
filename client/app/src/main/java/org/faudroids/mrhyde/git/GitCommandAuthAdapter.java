package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.faudroids.mrhyde.auth.LoginManager;

import javax.inject.Inject;

/**
 * Adds authentication to a {@link org.eclipse.jgit.api.TransportCommand}.
 */
public class GitCommandAuthAdapter {

  private final LoginManager loginManager;

  @Inject
  GitCommandAuthAdapter(LoginManager loginManager) {
    this.loginManager = loginManager;
  }

  public <T extends TransportCommand<T, ?>> T wrap(@NonNull T command) {
    return command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
        loginManager.getAccount().getLogin(),
        loginManager.getAccount().getAccessToken()
    ));
  }

}
