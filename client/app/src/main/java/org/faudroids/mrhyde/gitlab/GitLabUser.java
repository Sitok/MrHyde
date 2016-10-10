package org.faudroids.mrhyde.gitlab;

import com.google.gson.annotations.SerializedName;

/**
 * GitLab API user object.
 */
public class GitLabUser {

  private String username;
  private String email;
  @SerializedName("avatar_url")
  private String avatarUrl;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
}
