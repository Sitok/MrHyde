package org.faudroids.mrhyde.github;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Responsible for storing and clearing (e.g. on logout) the GitHub user credentials.
 */
@Singleton
public final class LoginManager {

  private static final String
      KEY_ACCESS_TOKEN = "ACCESS_TOKEN",
      KEY_LOGIN = "LOGIN",
      KEY_EMAIL = "EMAIL";

  private final Context context;
  private Account accountCache = null;

  @Inject
  LoginManager(Context context) {
    this.context = context;
  }


  public void setAccount(Account account) {
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.putString(KEY_ACCESS_TOKEN, account.getAccessToken());
    editor.putString(KEY_LOGIN, account.getLogin());
    editor.putString(KEY_EMAIL, account.getEmail());
    editor.commit();
    accountCache = account;
  }


  public Account getAccount() {
    if (accountCache == null) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      if (!prefs.contains(KEY_ACCESS_TOKEN)) return null;
      accountCache = new Account(
          prefs.getString(KEY_ACCESS_TOKEN, null),
          prefs.getString(KEY_LOGIN, null),
          prefs.getString(KEY_EMAIL, null)
      );
    }
    return accountCache;
  }


  @SuppressWarnings("deprecation")
  public void clearAccount() {
    // clear local credentials
    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    editor.remove(KEY_ACCESS_TOKEN);
    editor.remove(KEY_LOGIN);
    editor.remove(KEY_EMAIL);
    editor.commit();

    // clear credentials stored in cookies
    CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.removeAllCookie();
  }


  public static final class Account {

    private final String accessToken, login, email;

    public Account(String accessToken, String login, String email) {
      this.accessToken = accessToken;
      this.login = login;
      this.email = email;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public String getLogin() {
      return login;
    }

    public String getEmail() {
      return email;
    }

  }

}
