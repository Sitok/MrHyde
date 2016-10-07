package org.faudroids.mrhyde.github;

import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;

public class GitHubAccount extends Account {

  public GitHubAccount(String accessToken, String login, String email) {
    super(accessToken, login, email);
  }

  @Override
  public <P, R> R accept(AccountVisitor<P, R> visitor, P param) {
    return visitor.visit(this, param);
  }

}

