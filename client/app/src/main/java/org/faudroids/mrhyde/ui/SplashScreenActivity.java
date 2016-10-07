package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.os.Bundle;

import org.faudroids.mrhyde.app.MigrationManager;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;

import javax.inject.Inject;


/**
 * Empty activity for showing the splash screen (activity background)
 */
public final class SplashScreenActivity extends AbstractActivity {

  @Inject MigrationManager migrationManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((MrHydeApp) getApplication()).getComponent().inject(this);

    // start migration if necessary
    migrationManager.doMigration();

    startActivity(new Intent(this, ClonedReposActivity.class));
    finish();
  }


}
