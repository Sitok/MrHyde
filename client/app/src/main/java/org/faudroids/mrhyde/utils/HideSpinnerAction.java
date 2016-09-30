package org.faudroids.mrhyde.utils;


import org.faudroids.mrhyde.ui.utils.AbstractActivity;

public class HideSpinnerAction extends AbstractErrorAction {

  private final AbstractActivity activity;

  public HideSpinnerAction(AbstractActivity activity) {
    this.activity = activity;
  }

  @Override
  protected void doCall(Throwable throwable) {
    if (activity != null && activity.isSpinnerVisible()) activity.hideSpinner();
  }

}
