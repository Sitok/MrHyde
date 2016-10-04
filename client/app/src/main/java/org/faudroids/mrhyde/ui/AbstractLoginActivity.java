package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import butterknife.ButterKnife;
import rx.Observable;


/**
 * Base class for OAuth style login.
 */
abstract class AbstractLoginActivity extends AbstractActivity {

  static class NotAuthenticatedException extends Exception {}

  abstract Intent getTargetIntent();
  abstract @StringRes int getLoginDialogTitle();
  abstract String getLoginUrl();
  abstract Optional<String> getCodeFromUrl(String url) throws NotAuthenticatedException;
  abstract Observable<Account> getAccountFromCode(String accessCode);

  abstract Account getStoredAccount();
  abstract void storeAccount(Account account);


  private MaterialDialog loginDialog = null;
  private WebView loginView = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);

    // check if logged in
    if (getStoredAccount() != null) {
      onLoginSuccess();
      return;
    }

    // check for interrupted login attempt
    if (savedInstanceState != null) {
      startLogin(savedInstanceState);
    } else {
      startLogin(null);
    }
  }

  @Override
  public void onDestroy() {
    hideDialog();
    super.onDestroy();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (loginView != null) loginView.saveState(outState);
    super.onSaveInstanceState(outState);
  }

  private void startLogin(Bundle savedState) {
    loginDialog = new MaterialDialog.Builder(this)
        .title(getLoginDialogTitle())
        .customView(R.layout.dialog_login, false)
        .cancelListener(dialogInterface -> finish())
        .show();

    loginView = (WebView) loginDialog.getCustomView().findViewById(R.id.webview);
    if (savedState != null) {
      loginView.restoreState(savedState);
    } else {
      loginView.loadUrl(getLoginUrl());
    }
    loginView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        try {
          Optional<String> code = getCodeFromUrl(url);
          if (!code.isPresent()) return;
          getAccessToken(code.get());
        } catch (NotAuthenticatedException e) {
          onAccessDenied();
        }
      }
    });
  }

  private void getAccessToken(String code) {
    hideDialog();
    showSpinner();
    compositeSubscription.add(getAccountFromCode(code)
        .compose(new DefaultTransformer<>())
        .subscribe(account -> {
          storeAccount(account);
          onLoginSuccess();
        }, new ErrorActionBuilder()
            .add(new DefaultErrorAction(this, "failed to get token"))
            .add(new HideSpinnerAction(this))
            .build()));
  }

  private void onLoginSuccess() {
    startActivity(getTargetIntent());
    finish();
  }

  private void onAccessDenied() {
    hideDialog();
    new MaterialDialog.Builder(this)
        .title(R.string.login_error_title)
        .content(R.string.login_error_message)
        .positiveText(android.R.string.ok)
        .onAny((dialog, which) -> startLogin(null))
        .cancelListener(dialogInterface -> startLogin(null))
        .show();
  }

  private void hideDialog() {
    if (loginDialog == null) return;
    loginDialog.dismiss();
    loginDialog = null;
    loginView = null;
  }

}
