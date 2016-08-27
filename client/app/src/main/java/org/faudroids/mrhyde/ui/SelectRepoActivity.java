package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.os.Bundle;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.github.GitHubRepository;
import org.faudroids.mrhyde.ui.utils.AbstractActionBarActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.ButterKnife;
import rx.Observable;

public class SelectRepoActivity extends AbstractActionBarActivity {

  static final String RESULT_REPOSITORY = "RESULT_REPOSITORY";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
    setTitle(getString(R.string.title_select_repository));
    setContentView(R.layout.activity_select_repo);
    ButterKnife.bind(this);
  }


  protected void returnRepository(GitHubRepository repository) {
    Intent data = new Intent();
    data.putExtra(RESULT_REPOSITORY, repository);
    setResult(RESULT_OK, data);
    finish();
  }


  public static final class SelectRepoFragment extends AllReposFragment {

    @Override
    protected void loadRepositories() {
      showSpinner();
      compositeSubscription.add(
          Observable.zip(
              gitHubManager.getAllRepositories(),
              gitHubManager.getFavouriteRepositories(),
              (allRepos, favouriteRepos) -> {
                // show only not favourite repositories
                Collection<GitHubRepository> filteredRepos = new ArrayList<>();
                for (GitHubRepository repo : allRepos) {
                  boolean found = false;
                  for (GitHubRepository favouriteRepo : favouriteRepos) {
                    if (repo.getId() == favouriteRepo.getId()) {
                      found = true;
                      break;
                    }
                  }
                  if (!found) filteredRepos.add(repo);
                }
                return filteredRepos;
              })
              .compose(new DefaultTransformer<Collection<GitHubRepository>>())
              .subscribe(repositories -> {
                hideSpinner();
                repoAdapter.setItems(repositories);
              }, new ErrorActionBuilder()
                  .add(new DefaultErrorAction(this.getActivity(), "failed to get repos"))
                  .add(new HideSpinnerAction(this))
                  .build()));
    }

    @Override
    protected void onRepositorySelected(GitHubRepository repository) {
      ((SelectRepoActivity) getActivity()).returnRepository(repository);
    }

  }

}
