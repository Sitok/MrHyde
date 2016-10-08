package org.faudroids.mrhyde.ui.utils;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.analytics.Tracker;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;

import javax.inject.Inject;

import butterknife.BindView;
import rx.subscriptions.CompositeSubscription;


public abstract class AbstractActivity extends AppCompatActivity {

  public final CompositeSubscription compositeSubscription = new CompositeSubscription();
  @Inject protected UiUtils uiUtils;
  @BindView(R.id.spinner) protected View spinnerContainerView;
  @BindView(R.id.spinner_image) protected ImageView spinnerImageView;
  protected Tracker analyticsTracker;

  protected boolean showBackButton = true;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // show action bar back button
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null && showBackButton) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
    }

    analyticsTracker = ((MrHydeApp) getApplication()).getAnalyticsTracker();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // handle action bar back buttons
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    compositeSubscription.unsubscribe();
  }

  public void showSpinner() {
    uiUtils.showSpinner(spinnerContainerView, spinnerImageView);
  }

  public void hideSpinner() {
    uiUtils.hideSpinner(spinnerContainerView, spinnerImageView);
  }

  public boolean isSpinnerVisible() {
    return uiUtils.isSpinnerVisible(spinnerContainerView);
  }

}
