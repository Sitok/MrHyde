package org.faudroids.mrhyde.github;

import org.faudroids.mrhyde.auth.Account;

public class GitHubAccount extends Account {

  public GitHubAccount(String accessToken, String login, String email) {
    super(accessToken, login, email);
  }

}

