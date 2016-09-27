package org.faudroids.mrhyde.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.ui.utils.AbstractReposFragment;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.util.Collection;

import butterknife.BindView;

public final class FavouriteReposFragment extends AbstractReposFragment {

  private static final int REQUEST_SELECT_REPOSITORY = 42;

  @BindView(R.id.empty)
  protected TextView emptyView;
  @BindView(R.id.add)
  protected AddFloatingActionButton addButton;


  public FavouriteReposFragment() {
    super(R.layout.fragment_repos_favourite);
  }


  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    ((MrHydeApp) getActivity().getApplication()).getComponent().inject(this);
    super.onViewCreated(view, savedInstanceState);

    addButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivityForResult(new Intent(getActivity(), SelectRepoActivity.class), REQUEST_SELECT_REPOSITORY);
      }
    });
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != Activity.RESULT_OK) {
      super.onActivityResult(requestCode, resultCode, data);
      return;
    }
    switch (requestCode) {
      case REQUEST_SELECT_REPOSITORY:
        Repository repository = (Repository) data.getSerializableExtra(SelectRepoActivity.RESULT_REPOSITORY);
        gitHubManager.markRepositoryAsFavourite(repository);
        loadRepositories();
        return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }


  @Override
  protected void loadRepositories() {
    showSpinner();
    compositeSubscription.add(repositoriesManager.getClonedRepositories()
        .compose(new DefaultTransformer<Collection<Repository>>())
        .subscribe(repositories -> {
          hideSpinner();
          if (repositories.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
          } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            repoAdapter.setItems(repositories);
          }
        }, new ErrorActionBuilder()
            .add(new DefaultErrorAction(this.getActivity(), "failed to get favourite repos"))
            .add(new HideSpinnerAction(this))
            .build()));
  }

}
