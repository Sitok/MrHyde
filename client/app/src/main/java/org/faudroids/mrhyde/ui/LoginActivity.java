package org.faudroids.mrhyde.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MigrationManager;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.github.GitHubEmail;
import org.faudroids.mrhyde.github.GitHubAuthApi;
import org.faudroids.mrhyde.github.GitHubEmailsApi;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import timber.log.Timber;


public final class LoginActivity extends AbstractActivity {

	private static final String STATE_LOGIN_RUNNING = "STATE_LOGIN_RUNNING";
	private static final String GITHUB_LOGIN_STATE = UUID.randomUUID().toString();

	@BindView(R.id.login_button) protected Button loginButton;
	@Inject GitHubAuthApi gitHubAuthApi;
	@Inject GitHubEmailsApi gitHubEmailsApi;
	@Inject LoginManager loginManager;
	@Inject MigrationManager migrationManager;

	private Dialog loginDialog = null;
	private WebView loginView = null;
	private boolean loginRunning = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);
    ((MrHydeApp) getApplication()).getComponent().inject(this);

		// start migration if necessary
		migrationManager.doMigration();

		// check if logged in
		if (loginManager.getAccount() != null) {
			onLoginSuccess();
			return;
		}

		// setup UI
		loginButton.setOnClickListener(arg0 -> {
      loginRunning = true;
      startLogin(null);
    });

		// check for interrupted login attempt
		if (savedInstanceState != null) {
			loginRunning = savedInstanceState.getBoolean(STATE_LOGIN_RUNNING);
			if (loginRunning) {
				startLogin(savedInstanceState);
			}
		}
	}


	@Override
	public void onDestroy() {
		hideDialog();
		super.onDestroy();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_LOGIN_RUNNING, loginRunning);
		if (loginView != null) loginView.saveState(outState);
		super.onSaveInstanceState(outState);
	}


	private void startLogin(Bundle savedState) {
		loginDialog = new Dialog(LoginActivity.this);
		loginDialog.setContentView(R.layout.dialog_login);
		loginView = (WebView) loginDialog.findViewById(R.id.webview);
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

					hideDialog();
					getAccessToken(code);

				} else if (url.contains("error=access_denied")) {
					hideDialog();
					onAccessDenied();
				}
			}
		});
		loginDialog.show();
		loginDialog.setTitle(getString(R.string.login_title));
		loginDialog.setCancelable(true);
	}


	private void getAccessToken(String code) {
		String clientId = getString(R.string.gitHubClientId);
		String clientSecret = getString(R.string.gitHubClientSecret);
		showSpinner();
		compositeSubscription.add(gitHubAuthApi.getAccessToken(clientId, clientSecret, code)
				.flatMap(tokenDetails -> {
          try {
            // load user
            UserService userService = new UserService();
            userService.getClient().setOAuth2Token(tokenDetails.getAccessToken());
            User user = userService.getUser();
            List<GitHubEmail> emails = gitHubEmailsApi.getEmails(tokenDetails.getAccessToken());
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
          Timber.d("gotten token " + account.getAccessToken());
          loginManager.setAccount(account);
          onLoginSuccess();
        }, new ErrorActionBuilder()
						.add(new DefaultErrorAction(this, "failed to get token"))
						.add(new HideSpinnerAction(this))
						.build()));
	}


	private void onLoginSuccess() {
		startActivity(new Intent(LoginActivity.this, ClonedReposActivity.class));
		finish();
	}


	private void onAccessDenied() {
		new MaterialDialog.Builder(this)
				.title(R.string.login_error_title)
				.content(R.string.login_error_message)
				.positiveText(android.R.string.ok)
				.show();
	}


	private void hideDialog() {
		if (loginDialog == null) return;
		loginDialog.dismiss();
		loginDialog = null;
		loginView = null;
	}

}
