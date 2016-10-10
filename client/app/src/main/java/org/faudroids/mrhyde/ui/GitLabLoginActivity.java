package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.common.base.Optional;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.gitlab.GitLabAccount;
import org.faudroids.mrhyde.gitlab.GitLabApi;

import java.util.UUID;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;


public final class GitLabLoginActivity extends AbstractLoginActivity {

  private static final String LOGIN_STATE = UUID.randomUUID().toString();

  @Inject GitLabApi gitLabApi;
  @Inject LoginManager loginManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
  }

  @Override
  Intent getTargetIntent() {
    return new Intent(this, CloneGitLabRepoActivity.class);
  }

  @Override
  int getActivityTitle() {
    return R.string.login_with_gitlab_title;
  }

  @Override
  String getLoginUrl() {
    return "https://gitlab.com/oauth/authorize?"
        + "&client_id=" + getString(R.string.gitLabClientId)
        + "&response_type=code"
        + "&redirect_uri=http://localhost"
        + "&state=" + LOGIN_STATE;
  }

  @Override
  Optional<String> getCodeFromUrl(String url) throws NotAuthenticatedException {
    if (url.contains("code=")) {
      Uri uri = Uri.parse(url);

      if (!LOGIN_STATE.equals(uri.getQueryParameter("state"))) {
        Timber.w("GitLab login states did not match");
        throw new NotAuthenticatedException();
      }

      String code = uri.getQueryParameter("code");
      return Optional.of(code);
    }
    return Optional.absent();
  }

  @Override
  Observable<Account> getAccountFromCode(String accessCode) {
    return gitLabApi
        .getAccessToken(accessCode)
        .flatMap(gitLabToken -> {
          // tmp set account with credentials for authentication
          loginManager.setGitLabAccount(new GitLabAccount(gitLabToken.getRefreshToken(), "", ""));
          return gitLabApi
              .getUser()
              .map(user -> new GitLabAccount(
                  loginManager.getGitLabAccount().getRefreshToken(),
                  user.getUsername(),
                  user.getAvatarUrl()
              ));
        });
  }

  @Override
  Account getStoredAccount() {
    return loginManager.getGitLabAccount();
  }

  @Override
  void storeAccount(Account account) {
    loginManager.setGitLabAccount((GitLabAccount) account);
  }

}
