package org.faudroids.mrhyde.ui;

import android.os.Bundle;
import android.view.View;

import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.ui.utils.AbstractReposFragment;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.util.Collection;

public class AllReposFragment extends AbstractReposFragment {

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    ((MrHydeApp) getActivity().getApplication()).getComponent().inject(this);
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  protected void loadRepositories() {
    showSpinner();
    compositeSubscription.add(gitHubManager.getAllRepositories()
        .compose(new DefaultTransformer<Collection<Repository>>())
        .subscribe(repositories -> {
          hideSpinner();
          repoAdapter.setItems(repositories);
        }, new ErrorActionBuilder()
            .add(new DefaultErrorAction(this.getActivity(), "failed to get repos"))
            .add(new HideSpinnerAction(this))
            .build()));
  }

}
