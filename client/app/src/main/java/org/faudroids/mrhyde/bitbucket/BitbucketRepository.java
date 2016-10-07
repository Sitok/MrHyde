package org.faudroids.mrhyde.bitbucket;

import java.util.List;

/**
 * Bitbucket API repository.
 */
public class BitbucketRepository {

  private String scm;
  private String name;
  private BitbucketUser owner;
  private Links links;

  public String getScm() {
    return scm;
  }

  public void setScm(String scm) {
    this.scm = scm;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BitbucketUser getOwner() {
    return owner;
  }

  public void setOwner(BitbucketUser owner) {
    this.owner = owner;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public static class Links {

    private List<BitbucketLink> clone;

    public List<BitbucketLink> getClone() {
      return clone;
    }

    public void setClone(List<BitbucketLink> clone) {
      this.clone = clone;
    }
  }
}
