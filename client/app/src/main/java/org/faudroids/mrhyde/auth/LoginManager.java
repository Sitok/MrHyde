package org.faudroids.mrhyde.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.webkit.CookieManager;

import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.github.GitHubAccount;
import org.faudroids.mrhyde.gitlab.GitLabAccount;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Responsible for storing and clearing (e.g. on logout) the GitHub user credentials.
 */
@Singleton
public final class LoginManager {

  private static final String
      GITHUB_KEY_ACCESS_TOKEN = "ACCESS_TOKEN",
      GITHUB_KEY_LOGIN = "LOGIN",
      GITHUB_KEY_EMAIL = "EMAIL";

  private static final String
      BITBUCKET_KEY_REFRESH_TOKEN = "BITBUCKET_REFRESH_TOKEN",
      BITBUCKET_KEY_LOGIN = "BITBUCKET_LOGIN",
      BITBUCKET_KEY_EMAIL = "BITBUCKET_EMAIL";

  private static final String
      GITLAB_KEY_PERSONAL_ACCESS_TOKEN = "GITLAB_PERSONAL_ACCESS_TOKEN",
      GITLAB_KEY_LOGIN = "GITLAB_LOGIN",
      GITLAB_KEY_EMAIL = "GITLAB_EMAIL";

  private final Context context;
  private GitHubAccount gitHubAccountCache = null;
  private BitbucketAccount bitbucketAccountCache = null;
  private GitLabAccount gitLabAccountCache = null;

  @Inject
  LoginManager(Context context) {
    this.context = context;
  }

  public void setGitHubAccount(GitHubAccount account) {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putString(GITHUB_KEY_ACCESS_TOKEN, account.getAccessToken());
    editor.putString(GITHUB_KEY_LOGIN, account.getLogin());
    editor.putString(GITHUB_KEY_EMAIL, account.getEmail());
    editor.commit();
    gitHubAccountCache = account;
  }

  public GitHubAccount getGitHubAccount() {
    if (gitHubAccountCache == null) {
      SharedPreferences prefs = getPrefs();
      if (!prefs.contains(GITHUB_KEY_ACCESS_TOKEN)) return null;
      gitHubAccountCache = new GitHubAccount(
          prefs.getString(GITHUB_KEY_ACCESS_TOKEN, null),
          prefs.getString(GITHUB_KEY_LOGIN, null),
          prefs.getString(GITHUB_KEY_EMAIL, null)
      );
    }
    return gitHubAccountCache;
  }

  public void clearGitHubAccount() {
    // clear local credentials
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.remove(GITHUB_KEY_ACCESS_TOKEN);
    editor.remove(GITHUB_KEY_LOGIN);
    editor.remove(GITHUB_KEY_EMAIL);
    editor.commit();
    gitHubAccountCache = null;
    clearCookies();
  }

  public void setBitbucketAccount(BitbucketAccount account) {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putString(BITBUCKET_KEY_REFRESH_TOKEN, account.getRefreshToken());
    editor.putString(BITBUCKET_KEY_LOGIN, account.getLogin());
    editor.putString(BITBUCKET_KEY_EMAIL, account.getEmail());
    editor.commit();
    bitbucketAccountCache = account;
  }

  public BitbucketAccount getBitbucketAccount() {
    if (bitbucketAccountCache == null) {
      SharedPreferences prefs = getPrefs();
      if (!prefs.contains(BITBUCKET_KEY_REFRESH_TOKEN)) return null;
      bitbucketAccountCache = new BitbucketAccount(
          prefs.getString(BITBUCKET_KEY_REFRESH_TOKEN, null),
          prefs.getString(BITBUCKET_KEY_LOGIN, null),
          prefs.getString(BITBUCKET_KEY_EMAIL, null)
      );
    }
    return bitbucketAccountCache;
  }

  public void clearBitbucketAccount() {
    // clear local credentials
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.remove(BITBUCKET_KEY_REFRESH_TOKEN);
    editor.remove(BITBUCKET_KEY_LOGIN);
    editor.remove(BITBUCKET_KEY_EMAIL);
    editor.commit();
    bitbucketAccountCache = null;
    clearCookies();
  }

  public GitLabAccount getGitLabAccount() {
    if (gitLabAccountCache == null) {
      SharedPreferences prefs = getPrefs();
      if (!prefs.contains(GITLAB_KEY_PERSONAL_ACCESS_TOKEN)) return null;
      gitLabAccountCache = new GitLabAccount(
          prefs.getString(GITLAB_KEY_PERSONAL_ACCESS_TOKEN, null),
          prefs.getString(GITLAB_KEY_LOGIN, null),
          prefs.getString(GITLAB_KEY_EMAIL, null)
      );
    }
    return gitLabAccountCache;
  }

  public void clearGitLabAccount() {
    // clear local credentials
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.remove(GITLAB_KEY_PERSONAL_ACCESS_TOKEN);
    editor.remove(GITLAB_KEY_LOGIN);
    editor.remove(GITLAB_KEY_EMAIL);
    editor.commit();
    gitLabAccountCache = null;
    clearCookies();
  }

  public void setGitLabAccount(GitLabAccount account) {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.putString(GITLAB_KEY_PERSONAL_ACCESS_TOKEN, account.getPersonalAccessToken());
    editor.putString(GITLAB_KEY_LOGIN, account.getLogin());
    editor.putString(GITLAB_KEY_EMAIL, account.getEmail());
    editor.commit();
    gitLabAccountCache = account;
  }

  public Account getAccount(@NonNull Repository repository) {
    switch (repository.getAuthType()) {
      case GITHUB_OAUTH2_ACCESS_TOKEN:
        return getGitHubAccount();
      case BITBUCKET_OAUTH2_ACCESS_TOKEN:
        return getBitbucketAccount();
      case GITLAB_OAUTH2_ACCESS_TOKEN:
        return getGitLabAccount();
      default:
        throw new IllegalArgumentException("Unknown auth type " + repository.getAuthType());
    }
  }


  @SuppressWarnings("deprecation")
  private void clearCookies() {
    // clear credentials stored in cookies
    CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.removeAllCookie();
  }

  private SharedPreferences getPrefs() {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }


}
