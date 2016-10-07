package org.faudroids.mrhyde.ui;


import android.os.Bundle;

import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.bitbucket.BitbucketManager;
import org.faudroids.mrhyde.git.Repository;

import java.util.Collection;

import javax.inject.Inject;

import rx.Observable;

/**
 * Displays a list of all available Bitbucket repositories.
 */
public class CloneBitbucketRepoActivity extends AbstractCloneRepoActivity {

  @Inject protected BitbucketManager bitbucketManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
  }

  @Override
  Observable<Collection<Repository>> getAllRemoteRepositories() {
    return bitbucketManager.getAllRepositories();
  }
}
