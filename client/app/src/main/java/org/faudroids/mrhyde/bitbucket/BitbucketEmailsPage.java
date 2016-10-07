package org.faudroids.mrhyde.bitbucket;

import java.util.List;

/**
 * Bitbucket API pagination for {@link BitbucketEmail}.
 */
public class BitbucketEmailsPage {

  private int pagelen;
  private List<BitbucketEmail> values;

  public int getPagelen() {
    return pagelen;
  }

  public void setPagelen(int pagelen) {
    this.pagelen = pagelen;
  }

  public List<BitbucketEmail> getValues() {
    return values;
  }

  public void setValues(List<BitbucketEmail> values) {
    this.values = values;
  }
}
