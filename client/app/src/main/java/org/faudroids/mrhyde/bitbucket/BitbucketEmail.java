package org.faudroids.mrhyde.bitbucket;

import com.google.gson.annotations.SerializedName;

/**
 * Bitbucket email API object.
 */
public class BitbucketEmail {

  @SerializedName("is_primary")
  private boolean isPrimary;
  @SerializedName("is_confirmed")
  private boolean isConfirmed;
  private String type;
  private String email;

  public boolean isPrimary() {
    return isPrimary;
  }

  public void setPrimary(boolean primary) {
    isPrimary = primary;
  }

  public boolean isConfirmed() {
    return isConfirmed;
  }

  public void setConfirmed(boolean confirmed) {
    isConfirmed = confirmed;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
