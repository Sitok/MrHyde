package org.faudroids.mrhyde.gitlab;

import com.google.common.base.Objects;

import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;

/**
 * GitLab {@link Account}.
 */
public class GitLabAccount extends Account {

  private final String personalAccessToken;

  public GitLabAccount(String personalAccessToken, String login, String email) {
    super(login, email);
    this.personalAccessToken = personalAccessToken;
  }

  public String getPersonalAccessToken() {
    return personalAccessToken;
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
    return Objects.equal(personalAccessToken, that.personalAccessToken);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), personalAccessToken);
  }
}
