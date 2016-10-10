package org.faudroids.mrhyde.gitlab;

/**
 * GitLab project owner API object.
 */
public class GitLabProjectOwner {

  private String id;
  private String name;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
