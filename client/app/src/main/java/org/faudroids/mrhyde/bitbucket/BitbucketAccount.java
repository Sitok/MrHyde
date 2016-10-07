package org.faudroids.mrhyde.bitbucket;

import com.google.common.base.Objects;

import org.faudroids.mrhyde.auth.Account;

/**
 * Regular {@link org.faudroids.mrhyde.auth.Account}, except it also
 * stores the refresh token. Bitbucket access tokens expire every 60 minutes! :(
 */
public class BitbucketAccount extends Account {

  private final String refreshToken;

  public BitbucketAccount(String accessToken, String refreshToken, String login, String email) {
    super(accessToken, login, email);
    this.refreshToken = refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BitbucketAccount)) return false;
    if (!super.equals(o)) return false;
    BitbucketAccount that = (BitbucketAccount) o;
    return Objects.equal(refreshToken, that.refreshToken);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), refreshToken);
  }
}
