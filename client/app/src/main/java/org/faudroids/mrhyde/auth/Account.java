package org.faudroids.mrhyde.auth;

import com.google.common.base.Objects;

/**
 * A single authenticated account (e.g. GitHub, Bitbucket, etc.).
 */
public abstract class Account {

  private final String accessToken, login, email;

  public Account(String accessToken, String login, String email) {
    this.accessToken = accessToken;
    this.login = login;
    this.email = email;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getLogin() {
    return login;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Account)) return false;
    Account account = (Account) o;
    return Objects.equal(accessToken, account.accessToken) &&
        Objects.equal(login, account.login) &&
        Objects.equal(email, account.email);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(accessToken, login, email);
  }
}
