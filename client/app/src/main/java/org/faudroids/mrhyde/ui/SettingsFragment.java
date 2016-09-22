package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.github.LoginManager;
import org.faudroids.mrhyde.ui.utils.AbstractFragment;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;

public class SettingsFragment extends AbstractFragment {

  @BindView(R.id.versionTextView) protected TextView version;
  @BindView(R.id.authorTextView) protected TextView authors;
  @BindView(R.id.creditsTextView) protected TextView credits;
  @BindView(R.id.logoutTextView) protected TextView logout;

  @Inject
  LoginManager loginManager;


  public SettingsFragment() {
    super(R.layout.fragment_setting);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ((MrHydeApp) getActivity().getApplication()).getComponent().inject(this);

    version.setText(getVersion());
    setOnClickDialogForTextView(authors, R.string.about, R.string.about_msg);
    setOnClickDialogForTextView(credits, R.string.credits, R.string.credits_msg);

    logout.setOnClickListener(v -> {
      loginManager.clearAccount();
      getActivity().finish();
      startActivity(new Intent(getActivity(), LoginActivity.class));
    });
  }

  private String getVersion() {
    try {
      return getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
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
        .Builder(getActivity())
        .title(titleResourceId)
        .content(Html.fromHtml("<font color='#000000'>" + getString(msgResourceId) + "</font>"))
        .positiveText(android.R.string.ok);

    textView.setOnClickListener(v -> dialogBuilder.show());
  }

}
