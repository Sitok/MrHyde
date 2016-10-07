package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.common.base.Optional;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.bitbucket.BitbucketApi;

import javax.inject.Inject;

import rx.Observable;


public final class BitbucketLoginActivity extends AbstractLoginActivity {

  @Inject BitbucketApi bitbucketApi;
  @Inject LoginManager loginManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
  }

  @Override
  Intent getTargetIntent() {
    return new Intent(this, ClonedReposActivity.class);
  }

  @Override
  int getLoginDialogTitle() {
    return R.string.login_with_bitbucket_title;
  }

  @Override
  String getLoginUrl() {
    return "https://bitbucket.org/site/oauth2/authorize?"
        + "&client_id=" + getString(R.string.bitbucketClientId)
        + "&response_type=code";
  }

  @Override
  Optional<String> getCodeFromUrl(String url) throws NotAuthenticatedException {
    if (url.contains("code=")) {
      Uri uri = Uri.parse(url);
      String code = uri.getQueryParameter("code");
      return Optional.of(code);
    }
    return Optional.absent();
  }

  @Override
  Observable<Account> getAccountFromCode(String accessCode) {
    return bitbucketApi
        .getAccessToken(accessCode)
        .map(token -> new BitbucketAccount(token.getAccessToken(), "none", "Superman", "superman@home.de"));
  }

  @Override
  Account getStoredAccount() {
    return loginManager.getBitbucketAccount();
  }

  @Override
  void storeAccount(Account account) {
    loginManager.setBitbucketAccount((BitbucketAccount) account);
  }

}
