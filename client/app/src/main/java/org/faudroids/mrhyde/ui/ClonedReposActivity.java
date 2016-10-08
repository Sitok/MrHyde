package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.RepositoriesManager;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClonedReposActivity
    extends AbstractActivity
    implements RepositoryRecyclerViewAdapter.RepositorySelectionListener {

  private static final int REQUEST_OVERVIEW = 41; // used to mark the end of an overview activity

  @Inject protected RepositoriesManager repositoriesManager;
  @Inject protected ActivityIntentFactory intentFactory;

  @BindView(R.id.list) protected RecyclerView recyclerView;
  protected RepositoryRecyclerViewAdapter repoAdapter;
  @BindView(R.id.empty) protected TextView emptyView;
  @BindView(R.id.add) protected FloatingActionsMenu cloneNewRepoBtn;
  @BindView(R.id.add_github) protected FloatingActionButton githubLoginBtn;
  @BindView(R.id.add_bitbucket) protected FloatingActionButton bitbucketLoginBtn;
  @BindView(R.id.tint) protected View tintView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    showBackButton = false;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cloned_repos);
    ButterKnife.bind(this);
    setTitle(Html.fromHtml("<b>" + getString(R.string.app_name) + "</b>"));

    // setup list
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    repoAdapter = new RepositoryRecyclerViewAdapter(this, this);
    recyclerView.setAdapter(repoAdapter);
    loadRepositories();

    // setup clone new repo btn
    cloneNewRepoBtn.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
      @Override
      public void onMenuExpanded() {
        tintView.animate().alpha(1).setDuration(200).start();
      }
      @Override
      public void onMenuCollapsed() {
        tintView.animate().alpha(0).setDuration(200).start();
      }
    });
    tintView.setOnClickListener(v -> cloneNewRepoBtn.collapse());
    githubLoginBtn.setOnClickListener(view -> {
      cloneNewRepoBtn.collapse();
      startActivityForResult(
          new Intent(ClonedReposActivity.this, GitHubLoginActivity.class),
          REQUEST_OVERVIEW
      );
    });
    bitbucketLoginBtn.setOnClickListener(view -> {
      cloneNewRepoBtn.collapse();
      startActivityForResult(
          new Intent(ClonedReposActivity.this, BitbucketLoginActivity.class),
          REQUEST_OVERVIEW
      );
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_OVERVIEW:
        loadRepositories();
        return;
    }
  }

  private void loadRepositories() {
    showSpinner();
    compositeSubscription.add(repositoriesManager.getClonedRepositories()
        .compose(new DefaultTransformer<>())
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
            .add(new DefaultErrorAction(this, "failed to get cloned repos"))
            .add(new HideSpinnerAction(this))
            .build()));
  }

  @Override
  public void onRepositorySelected(Repository repository) {
    Intent repoIntent = intentFactory.createRepoOverviewIntent(repository);
    startActivityForResult(repoIntent, REQUEST_OVERVIEW);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_cloned_repos, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;

      case R.id.feedback:
        String address = getString(R.string.feedback_mail_address);
        String subject = getString(R.string.feedback_mail_subject);
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        Intent mailIntent = Intent.createChooser(intent, getString(R.string.feedback_mail_chooser));
        startActivity(mailIntent);
        return true;

    }
    return super.onOptionsItemSelected(item);
  }

}
