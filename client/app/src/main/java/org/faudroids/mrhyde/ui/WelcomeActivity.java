package org.faudroids.mrhyde.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MigrationManager;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends AbstractActivity {

  private final int REQUEST_GITHUB_LOGIN = 42;
  private final int REQUEST_BITBUCKET_LOGIN = 43;

  private final String PREFS_NAME = "PREFS_WELCOME_ACTIVITY";
  private final String KEY_WELCOME_SCREEN_VISITED = "WELCOME_SCREEN_VISITED";
  private final String KEY_MIGRATING_TO_V1 = "MIGRATING_TO_V1";

  @Inject MigrationManager migrationManager;

  @BindView(R.id.btn_github) protected View githubButton;
  @BindView(R.id.btn_bitbucket) protected View bitbucketButton;
  @BindView(R.id.txt_import) protected View importTextView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);

    // start migration if necessary
    migrationManager.doMigration();

    // if already visited, skip this screen
    boolean welcomeScreenVisited = getPrefs().getBoolean(KEY_WELCOME_SCREEN_VISITED, false);
    if (welcomeScreenVisited) {
      startActivity(new Intent(this, ClonedReposActivity.class));
      finish();
    }

    setContentView(R.layout.activity_welcome);
    ButterKnife.bind(this);

    // toggle note about importing repos
    boolean migratingToV1 = migrationManager.isMigratingToVersionName1();
    if (migratingToV1) getPrefs().edit().putBoolean(KEY_MIGRATING_TO_V1, true).commit();
    else migratingToV1 = getPrefs().getBoolean(KEY_MIGRATING_TO_V1, false);
    importTextView.setVisibility(migratingToV1 ? View.VISIBLE : View.GONE);

    // setup login buttons
    Bundle extras = new Bundle();
    extras.putBoolean(AbstractLoginActivity.EXTRA_DO_NOT_FORWARD_TO_NEXT_ACTIVITY, true);
    githubButton.setOnClickListener(view -> {
      Intent intent = new Intent(WelcomeActivity.this, GitHubLoginActivity.class);
      intent.putExtras(extras);
      startActivityForResult(intent, REQUEST_GITHUB_LOGIN);
    });
    bitbucketButton.setOnClickListener(view -> {
      Intent intent = new Intent(WelcomeActivity.this, BitbucketLoginActivity.class);
      intent.putExtras(extras);
      startActivityForResult(intent, REQUEST_BITBUCKET_LOGIN);
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != RESULT_OK) return;
    // launcher cloned repos activity, launch clone new repo activity and finish
    startActivity(new Intent(this, ClonedReposActivity.class));
    switch(requestCode) {
      case REQUEST_GITHUB_LOGIN:
        startActivity(new Intent(this, CloneGitHubRepoActivity.class));
        break;
      case REQUEST_BITBUCKET_LOGIN:
        startActivity(new Intent(this, CloneBitbucketRepoActivity.class));
        break;
    }
    getPrefs().edit().putBoolean(KEY_WELCOME_SCREEN_VISITED, true).commit();
    finish();
  }

  private SharedPreferences getPrefs() {
    return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
  }

}
