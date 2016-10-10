package org.faudroids.mrhyde.gitlab;

import com.google.gson.annotations.SerializedName;

public class GitLabToken {

  @SerializedName("access_token")
  private String accessToken;
  @SerializedName("expires_in")
  private int expiresIn;
  @SerializedName("refresh_token")
  private String refreshToken;
  @SerializedName("token_type")
  private String tokenType;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }
}
