package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.github.GitHubApi;
import org.faudroids.mrhyde.github.GitHubEmail;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import rx.Observable;
import timber.log.Timber;


public final class GitHubLoginActivity extends AbstractActivity {

  private static final String GITHUB_LOGIN_STATE = UUID.randomUUID().toString();

  @Inject GitHubApi gitHubApi;
  @Inject LoginManager loginManager;

  private MaterialDialog loginDialog = null;
  private WebView loginView = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);

    // check if logged in
    if (loginManager.getAccount() != null) {
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
        .title(R.string.login_with_github_title)
        .customView(R.layout.dialog_login, false)
        .cancelListener(dialogInterface -> finish())
        .show();

    loginView = (WebView) loginDialog.getCustomView().findViewById(R.id.webview);
    if (savedState != null) {
      loginView.restoreState(savedState);
    } else {
      loginView.loadUrl("https://github.com/login/oauth/authorize?"
          + "&client_id=" + getString(R.string.gitHubClientId)
          + "&scope=user%2Crepo"
          + "&state=" + GITHUB_LOGIN_STATE);
    }
    loginView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (url.contains("code=")) {
          Uri uri = Uri.parse(url);
          String code = uri.getQueryParameter("code");
          if (!GITHUB_LOGIN_STATE.equals(uri.getQueryParameter("state"))) {
            Timber.w("GitHub login states did not match");
            onAccessDenied();
            return;
          }
          getAccessToken(code);

        } else if (url.contains("error=access_denied")) {
          onAccessDenied();
        }
      }
    });
  }

  private void getAccessToken(String code) {
    hideDialog();
    showSpinner();
    compositeSubscription.add(gitHubApi.getAccessToken(code)
        .flatMap(tokenDetails -> {
          try {
            // load user
            UserService userService = new UserService();
            userService.getClient().setOAuth2Token(tokenDetails.getAccessToken());
            User user = userService.getUser();
            List<GitHubEmail> emails = gitHubApi.getEmails(tokenDetails.getAccessToken()).toBlocking().first();
            GitHubEmail primaryEmail = null;
            for (GitHubEmail email : emails) {
              if (email.isPrimary()) {
                primaryEmail = email;
                break;
              }
            }
            if (primaryEmail == null && !emails.isEmpty()) primaryEmail = emails.get(0);
            String emailString = (primaryEmail == null) ? "dummy" : primaryEmail.getEmail();

            return Observable.just(new Account(tokenDetails.getAccessToken(), user.getLogin(), emailString));

          } catch (IOException e) {
            return Observable.error(e);
          }
        })
        .compose(new DefaultTransformer<Account>())
        .subscribe(account -> {
          loginManager.setAccount(account);
          onLoginSuccess();
        }, new ErrorActionBuilder()
            .add(new DefaultErrorAction(this, "failed to get token"))
            .add(new HideSpinnerAction(this))
            .build()));
  }

  private void onLoginSuccess() {
    startActivity(new Intent(GitHubLoginActivity.this, ClonedReposActivity.class));
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
