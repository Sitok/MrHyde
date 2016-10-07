package org.faudroids.mrhyde.github;

import com.google.common.base.Objects;

import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;

public class GitHubAccount extends Account {

  private final String accessToken;

  public GitHubAccount(String accessToken, String login, String email) {
    super(login, email);
    this.accessToken = accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public <P, R> R accept(AccountVisitor<P, R> visitor, P param) {
    return visitor.visit(this, param);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GitHubAccount)) return false;
    if (!super.equals(o)) return false;
    GitHubAccount that = (GitHubAccount) o;
    return Objects.equal(accessToken, that.accessToken);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), accessToken);
  }
}

