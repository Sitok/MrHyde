package org.faudroids.mrhyde.git;

import android.support.annotation.StringRes;

import org.faudroids.mrhyde.R;

/**
 * Supported hosting providers.
 */
public enum GitHostingProvider {

  GITHUB(R.string.github) {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitGitHub(param);
    }
  },
  BITBUCKET(R.string.bitbucket) {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitBitbucket(param);
    }
  },
  GITLAB(R.string.gitlab) {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitGitLab(param);
    }
  };

  @StringRes private final int nameRes;

  GitHostingProvider(@StringRes int nameRes) {
    this.nameRes = nameRes;
  }

  public abstract <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param);

  public int getNameRes() {
    return nameRes;
  }

}
