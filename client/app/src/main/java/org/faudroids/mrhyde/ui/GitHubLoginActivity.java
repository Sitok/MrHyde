package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.common.base.Optional;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.github.GitHubAccount;
import org.faudroids.mrhyde.github.GitHubApi;
import org.faudroids.mrhyde.github.GitHubEmail;
import org.faudroids.mrhyde.utils.ObservableUtils;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;


public final class GitHubLoginActivity extends AbstractLoginActivity<GitHubAccount> {

  private static final String GITHUB_LOGIN_STATE = UUID.randomUUID().toString();

  @Inject GitHubApi gitHubApi;
  @Inject LoginManager loginManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
  }

  @Override
  Intent getTargetIntent() {
    return new Intent(this, CloneGitHubRepoActivity.class);
  }

  @Override
  int getLoginDialogTitle() {
    return R.string.login_with_github_title;
  }

  @Override
  String getLoginUrl() {
    return "https://github.com/login/oauth/authorize?"
        + "&client_id=" + getString(R.string.gitHubClientId)
        + "&scope=user%2Crepo"
        + "&state=" + GITHUB_LOGIN_STATE;
  }

  @Override
  Optional<String> getCodeFromUrl(String url) throws NotAuthenticatedException {
    if (url.contains("code=")) {
      Uri uri = Uri.parse(url);
      String code = uri.getQueryParameter("code");
      if (!GITHUB_LOGIN_STATE.equals(uri.getQueryParameter("state"))) {
        Timber.w("GitHub login states did not match");
        throw new NotAuthenticatedException();
      }
      return Optional.of(code);

    } else if (url.contains("error=access_denied")) {
      throw new NotAuthenticatedException();
    }
    return Optional.absent();
  }

  @Override
  Observable<GitHubAccount> getAccountFromCode(String accessCode) {
    return gitHubApi.getAccessToken(accessCode)
        .flatMap(tokenDetails -> ObservableUtils.fromSynchronousCall(() -> {
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

          return new GitHubAccount(tokenDetails.getAccessToken(), user.getLogin(), emailString);
        }));
  }

  @Override
  GitHubAccount getStoredAccount() {
    return loginManager.getGitHubAccount();
  }

  @Override
  void storeAccount(GitHubAccount account) {
    loginManager.setGitHubAccount(account);
  }

}
