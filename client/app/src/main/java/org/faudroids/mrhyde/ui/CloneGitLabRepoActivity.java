package org.faudroids.mrhyde.ui;


import android.os.Bundle;

import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.gitlab.GitLabManager;

import java.util.Collection;

import javax.inject.Inject;

import rx.Observable;

/**
 * Displays a list of all available Bitbucket repositories.
 */
public class CloneGitLabRepoActivity extends AbstractCloneRepoActivity {

  @Inject protected GitLabManager gitLabManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
  }

  @Override
  Observable<Collection<Repository>> getAllRemoteRepositories() {
    return gitLabManager.getAllRepositories();
  }
}
