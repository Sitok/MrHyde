package org.faudroids.mrhyde.bitbucket;

import com.google.gson.annotations.SerializedName;

/**
 * Bitbucket API user object.
 */
public class BitbucketUser {

  private String username;
  @SerializedName("display_name")
  private String displayName;
  private Links links;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public static class Links {
    private BitbucketLink avatar;

    public BitbucketLink getAvatar() {
      return avatar;
    }

    public void setAvatar(BitbucketLink avatar) {
      this.avatar = avatar;
    }
  }
}
