package org.faudroids.mrhyde.ui;

import android.util.Pair;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.Branch;
import org.faudroids.mrhyde.git.GitManager;
import org.faudroids.mrhyde.github.GitHubRepository;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;

/**
 * Git related menu entries displayed in the action bar.
 */
public class GitActionBarMenu {

  public interface ActionsListener {
    void onRefreshContent();
  }

  private final AbstractActivity activity;
  private final ActionsListener actionsListener;
  private final GitManager gitManager;
  private final GitHubRepository repository;
  private final ActivityIntentFactory intentFactory;

  public GitActionBarMenu(
      AbstractActivity activity,
      ActionsListener actionsListener,
      GitManager gitManager,
      GitHubRepository repository,
      ActivityIntentFactory intentFactory) {

    this.activity = activity;
    this.actionsListener = actionsListener;
    this.gitManager = gitManager;
    this.repository = repository;
    this.intentFactory = intentFactory;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_commit:
        activity.startActivity(intentFactory.createCommitIntent(repository));
        return true;

      case R.id.action_preview:
        activity.startActivity(intentFactory.createPreviewIntent(repository));
        return true;

      case R.id.action_push:
        activity.showSpinner();
        activity.compositeSubscription.add(gitManager.push()
            .compose(new DefaultTransformer<>())
            .subscribe(aVoid -> {
                  activity.hideSpinner();
                  Toast.makeText(
                      activity,
                      activity.getString(R.string.push_success),
                      Toast.LENGTH_SHORT
                  ).show();
                },
                new ErrorActionBuilder()
                    .add(new DefaultErrorAction(activity, "Failed to push changes"))
                    .add(new HideSpinnerAction(activity))
                    .build()
            )
        );
        return true;

      case R.id.action_pull:
        activity.showSpinner();
        activity.compositeSubscription.add(gitManager.pull()
            .compose(new DefaultTransformer<>())
            .subscribe(aVoid -> {
                  activity.hideSpinner();
                  Toast.makeText(
                      activity,
                      activity.getString(R.string.pull_success),
                      Toast.LENGTH_SHORT
                  ).show();
                },
                new ErrorActionBuilder()
                    .add(new DefaultErrorAction(activity, "Failed to pull changes"))
                    .add(new HideSpinnerAction(activity))
                    .build()
            )
        );
        return true;

      case R.id.action_switch_branch:
        // show dialog for selecting branch
        Observable.zip(
            gitManager.listRemoteTrackingBranches(),
            gitManager.getCurrentBranchName(),
            Pair::new
        )
            .compose(new DefaultTransformer<>())
            .subscribe(branchInfo -> {

                  // all (remote) branches
                  List<String> branchNames = new ArrayList<String>();
                  for (Branch branch : branchInfo.first) branchNames.add(branch.getDisplayName());

                  // current branch
                  int currentBranchIdx = branchNames.indexOf(branchInfo.second);

                  Collections.sort(branchNames);
                  new MaterialDialog
                      .Builder(activity)
                      .title(R.string.switch_branch_title)
                      .items(branchNames)
                      .itemsCallbackSingleChoice(currentBranchIdx, (dialog, itemView, which, text) -> {
                        // checkout selected branch
                        Branch selectedBranch = branchInfo.first.get(which);
                        gitManager
                            .checkoutBranch(selectedBranch)
                            .compose(new DefaultTransformer<>())
                            .subscribe(aVoid -> {
                                  actionsListener.onRefreshContent();
                                  Toast.makeText(
                                      activity,
                                      activity.getString(R.string.switch_branch_success, selectedBranch.getDisplayName()),
                                      Toast.LENGTH_SHORT)
                                      .show();
                                },
                                new ErrorActionBuilder()
                                    .add(new DefaultErrorAction(activity, "Failed to checkout branch"))
                                    .build()
                            );
                        return true;
                      })
                      .show();
                },
                new ErrorActionBuilder()
                    .add(new DefaultErrorAction(activity, "Failed to list branches"))
                    .build()
            );
        return true;
    }
    return false;
  }
}
