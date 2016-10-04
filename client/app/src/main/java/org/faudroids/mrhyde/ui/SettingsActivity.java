package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.Html;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SettingsActivity extends AbstractActivity {

  @BindView(R.id.versionTextView) protected TextView version;
  @BindView(R.id.authorTextView) protected TextView authors;
  @BindView(R.id.creditsTextView) protected TextView credits;
  @BindView(R.id.logoutTextView) protected TextView logout;

  @Inject LoginManager loginManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    ButterKnife.bind(this);
    setTitle(R.string.settings);

    // set version
    version.setText(getVersion());
    setOnClickDialogForTextView(authors, R.string.about, R.string.about_msg);
    setOnClickDialogForTextView(credits, R.string.credits, R.string.credits_msg);

    // setup logout
    logout.setOnClickListener(v -> {
      loginManager.clearGitHubAccount();
      finish();
      startActivity(new Intent(SettingsActivity.this, GitHubLoginActivity.class));
    });
  }

  private String getVersion() {
    try {
      return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (PackageManager.NameNotFoundException nnfe) {
      Timber.e(nnfe, "failed to get version");
      return null;
    }
  }

  private void setOnClickDialogForTextView(
      TextView textView,
      @StringRes final int titleResourceId,
      @StringRes final int msgResourceId) {

    MaterialDialog.Builder dialogBuilder = new MaterialDialog
        .Builder(this)
        .title(titleResourceId)
        .content(Html.fromHtml("<font color='#000000'>" + getString(msgResourceId) + "</font>"))
        .positiveText(android.R.string.ok);

    textView.setOnClickListener(v -> dialogBuilder.show());
  }

}
