package org.faudroids.mrhyde.ui;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import com.afollestad.materialdialogs.MaterialDialog;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.gitlab.GitLabAccount;
import org.faudroids.mrhyde.gitlab.GitLabApi;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.utils.AbstractErrorAction;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class GitLabLoginActivity extends AbstractActivity {

  @Inject GitLabApi gitLabApi;
  @Inject LoginManager loginManager;
  @Inject ClipboardManager clipboardManager;

  @BindView(R.id.btn_confirm) ImageButton confirmBtn;
  @BindView(R.id.input_token) EditText tokenInput;
  @BindView(R.id.web_view) WebView webView;
  @BindView(R.id.btn_paste) ImageButton pasteBtn;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
    setTitle(R.string.login_with_gitlab_title);

    // check if logged in
    if (loginManager.getGitLabAccount() != null) {
      onLoginSuccess();
      return;
    }

    setContentView(R.layout.activity_login_gitlab);
    ButterKnife.bind(this);

    if (savedInstanceState == null) {
      new MaterialDialog.Builder(this)
          .title(R.string.gitlab_login_help_title)
          .content(R.string.gitlab_login_help_msg)
          .positiveText(android.R.string.ok)
          .show();
    }

    // navigate to personal access token website
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    webView.setWebViewClient(new WebViewClient());
    if (savedInstanceState != null) {
      webView.restoreState(savedInstanceState);
    } else {
      webView.loadUrl("https://gitlab.com/profile/personal_access_tokens");
    }

    // on confirm load profile info
    confirmBtn.setOnClickListener(view -> {
      String token = tokenInput.getText().toString();
      loginManager.setGitLabAccount(new GitLabAccount(token, "", ""));

      showSpinner();
      compositeSubscription.add(gitLabApi
          .getUser()
          .subscribe(user -> {
            loginManager.setGitLabAccount(new GitLabAccount(
                    loginManager.getGitLabAccount().getPersonalAccessToken(),
                    user.getUsername(),
                    user.getAvatarUrl()
                )
            );
            onLoginSuccess();
          }, new ErrorActionBuilder()
              .add(new DefaultErrorAction(this, "failed to get account"))
              .add(new HideSpinnerAction(this))
              .add(new AbstractErrorAction() {
                @Override
                protected void doCall(Throwable throwable) {
                  loginManager.clearGitLabAccount();
                }
              })
              .build()));
    });

    // listen to text changes and toggle confirm btn
    tokenInput.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        boolean isTokenEmpty = tokenInput.getText().toString().isEmpty();
        confirmBtn.setBackgroundResource(isTokenEmpty
            ? R.drawable.disabled_circular_selector
            : R.drawable.green_circular_selector);
        confirmBtn.setClickable(!isTokenEmpty);
      }

      @Override
      public void afterTextChanged(Editable editable) { }
    });

    // setup paste
    pasteBtn.setOnClickListener(view -> {
      Timber.d("On click");
      if (!clipboardManager.hasPrimaryClip()) return;
      Timber.d("Has primary clip");
      if (!clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) return;
      Timber.d("Is plain text");
      tokenInput.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText());
      Timber.d("Pasted");
    });
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (webView != null) webView.saveState(outState);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    super.onBackPressed();
  }

  private void onLoginSuccess() {
    boolean doNotForward = getIntent().getBooleanExtra(AbstractLoginActivity.EXTRA_DO_NOT_FORWARD_TO_NEXT_ACTIVITY, false);
    if (!doNotForward) {
      startActivity(new Intent(this, CloneGitLabRepoActivity.class));
    }
    setResult(RESULT_OK);
    finish();
  }

}
