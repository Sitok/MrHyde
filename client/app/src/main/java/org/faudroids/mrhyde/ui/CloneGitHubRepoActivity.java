package org.faudroids.mrhyde.ui;


import android.os.Bundle;

import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.github.GitHubManager;

import java.util.Collection;

import javax.inject.Inject;

import rx.Observable;

/**
 * Displays a list of all available GitHub repositories.
 */
public class CloneGitHubRepoActivity extends AbstractCloneRepoActivity {

  @Inject protected GitHubManager gitHubManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
  }

  @Override
  Observable<Collection<Repository>> getAllRemoteRepositories() {
    return gitHubManager.getAllRepositories();
  }
}
