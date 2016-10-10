package org.faudroids.mrhyde.git;

import android.support.annotation.DrawableRes;

import org.faudroids.mrhyde.R;

/**
 * Gets the avatar placeholder image for each {@link GitHostingProvider}.
 */
public class AvatarPlaceholder implements GitHostingProviderVisitor<Void, Integer> {

  @Override
  @DrawableRes
  public Integer visitGitHub(Void param) {
    return R.drawable.octocat_black;
  }

  @Override
  @DrawableRes
  public Integer visitBitbucket(Void param) {
    return R.drawable.bitbucket_black;
  }

  @Override
  @DrawableRes
  public Integer visitGitLab(Void param) {
    return R.drawable.gitlab_black;
  }

}
