package org.faudroids.mrhyde.git;

import android.support.annotation.StringRes;

import org.faudroids.mrhyde.R;

/**
 * Supported hosting providers.
 */
public enum GitHostingProvider {

  GITHUB(R.string.github, "github") {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitGitHub(param);
    }
  },
  BITBUCKET(R.string.bitbucket, "bitbucket") {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitBitbucket(param);
    }
  },
  GITLAB(R.string.gitlab, "gitlab") {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitGitLab(param);
    }
  };

  @StringRes private final int nameRes;
  private final String rootDirName;

  GitHostingProvider(@StringRes int nameRes, String rootDirName) {
    this.nameRes = nameRes;
    this.rootDirName = rootDirName;
  }

  public abstract <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param);

  public int getNameRes() {
    return nameRes;
  }

  public String getRootDirName() {
    return rootDirName;
  }

}
