package org.faudroids.mrhyde.gitlab;

import com.google.common.base.Objects;

import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;

/**
 * Regular {@link Account}, except it also
 * stores the refresh token. GitLab access tokens expire every now and then.
 */
public class GitLabAccount extends Account {

  private final String refreshToken;

  public GitLabAccount(String refreshToken, String login, String email) {
    super(login, email);
    this.refreshToken = refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  @Override
  public <P, R> R accept(AccountVisitor<P, R> visitor, P param) {
    return visitor.visit(this, param);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GitLabAccount)) return false;
    if (!super.equals(o)) return false;
    GitLabAccount that = (GitLabAccount) o;
    return Objects.equal(refreshToken, that.refreshToken);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), refreshToken);
  }
}
