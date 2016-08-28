package org.faudroids.mrhyde.ui.utils;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;

import org.faudroids.mrhyde.R;

import javax.inject.Inject;

public class UiUtils {

  @Inject
  UiUtils() {
  }


  public void showSpinner(View spinnerContainerView, ImageView spinnerImageView) {
    spinnerContainerView.setVisibility(View.VISIBLE);

    spinnerImageView.setBackgroundResource(R.drawable.spinner);
    AnimationDrawable animationDrawable = (AnimationDrawable) spinnerImageView.getBackground();
    animationDrawable.start();
  }


  public void hideSpinner(View spinnerContainerView, ImageView spinnerImageView) {
    AnimationDrawable animationDrawable = (AnimationDrawable) spinnerImageView.getBackground();
    animationDrawable.stop();

    spinnerContainerView.setVisibility(View.GONE);
  }


  public boolean isSpinnerVisible(View spinnerContainerView) {
    return spinnerContainerView.getVisibility() == View.VISIBLE;
  }

}
