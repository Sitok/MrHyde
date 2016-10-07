package org.faudroids.mrhyde.bitbucket;

import java.util.List;

/**
 * Bitbucket API pagination.
 */
public class BitbucketRepositoriesPage {

  private int pagelen;
  private List<BitbucketRepository> values;
  private String next;

  public int getPagelen() {
    return pagelen;
  }

  public List<BitbucketRepository> getValues() {
    return values;
  }

  public String getNext() {
    return next;
  }
}
