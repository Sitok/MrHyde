package org.faudroids.mrhyde.gitlab;

import com.google.gson.annotations.SerializedName;

/**
 * GitLab project API object.
 */
public class GitLabProject {

  private String name;
  @SerializedName("http_url_to_repo")
  private String httpUrlToRepo;
  private GitLabProjectOwner owner;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHttpUrlToRepo() {
    return httpUrlToRepo;
  }

  public void setHttpUrlToRepo(String httpUrlToRepo) {
    this.httpUrlToRepo = httpUrlToRepo;
  }

  public GitLabProjectOwner getOwner() {
    return owner;
  }

  public void setOwner(GitLabProjectOwner owner) {
    this.owner = owner;
  }
}
