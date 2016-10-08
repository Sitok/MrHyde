package org.faudroids.mrhyde.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.LoginManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class MigrationManager {

  private static final String PREFS_NAME = MigrationManager.class.getSimpleName();
  private static final String KEY_VERSION = "VERSION";

  private static final int NO_VERSION = -1;

  private final Context context;
  private final LoginManager loginManager;

  private boolean migratingToVersionName1 = false;

  @Inject
  MigrationManager(Context context, LoginManager loginManager) {
    this.context = context;
    this.loginManager = loginManager;
  }


  public void doMigration() {
    int currentVersionCode = getCurrentVersionCode();
    int storedVersionCode = getStoredVersionCode();
    storeVersionCode(currentVersionCode);

    if (storedVersionCode == currentVersionCode || storedVersionCode == NO_VERSION) {
      return;
    }

    Timber.i("migrating from " + storedVersionCode + " to " + currentVersionCode);
    if (storedVersionCode < 4 && currentVersionCode >= 4) {
      migrateToVersion4();
    }

    if (storedVersionCode <= 15 && currentVersionCode > 15) {
      migratingToVersionName1 = true;
    }
  }

  /**
   * @return true if migrating to version name 1 from a pre version 1 build
   */
  public boolean isMigratingToVersionName1() {
    return migratingToVersionName1;
  }


  /**
   * Version 4 fixes reading emails not just from the public GitHub profile,
   * but also from the private list of mails. If no mail is stored with the
   * {@link LoginManager} logout to ensure mail fetched during next login.
   */
  private void migrateToVersion4() {
    Account account = loginManager.getGitHubAccount();
    if (account != null && (account.getEmail() == null || account.getEmail().isEmpty())) {
      loginManager.clearGitHubAccount();
    }
  }


  private int getCurrentVersionCode() {
    try {
      PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return info.versionCode;
    } catch (PackageManager.NameNotFoundException nnfe) {
      Timber.e(nnfe, "failed to get package info");
      return NO_VERSION;
    }
  }


  private int getStoredVersionCode() {
    return context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_VERSION, NO_VERSION);
  }


  private void storeVersionCode(int versionCode) {
    SharedPreferences.Editor editor = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit();
    editor.putInt(KEY_VERSION, versionCode);
    editor.apply();
  }

}
