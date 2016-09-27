package org.faudroids.mrhyde.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.jgit.api.Status;
import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.GitManager;
import org.faudroids.mrhyde.git.GitManagerFactory;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import timber.log.Timber;

public final class CommitActivity extends AbstractActivity {

  static final String EXTRA_REPOSITORY = "EXTRA_REPOSITORY";

  private static final String
      STATE_FILES_EXPAND = "STATE_FILES_EXPAND",
      STATE_DIFF_EXPAND = "STATE_DIFF_EXPAND",
      STATE_MESSAGE_EXPAND = "STATE_MESSAGE_EXPAND";

  @BindView(R.id.changed_files_layout) protected View changedFilesLayout;
  @BindView(R.id.changed_files_expand) protected ImageView changedFilesExpandButton;
  @BindView(R.id.changed_files_title) protected TextView changedFilesTitleView;
  @BindView(R.id.changed_files) protected TextView changedFilesView;

  @BindView(R.id.diff_layout) protected View diffLayout;
  @BindView(R.id.diff_expand) protected ImageView diffExpandButton;
  @BindView(R.id.diff_title) protected TextView diffTitleView;
  @BindView(R.id.diff) protected TextView diffView;

  @BindView(R.id.message_layout) protected View messageLayout;
  @BindView(R.id.message_expand) protected ImageView messageExpandButton;
  @BindView(R.id.message_title) protected TextView messageTitleView;
  @BindView(R.id.message) protected EditText messageView;

  @BindView(R.id.commit_button) protected Button commitButton;

  @Inject GitManagerFactory gitManagerFactory;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_commit);
    ButterKnife.bind(this);

    setTitle(getString(R.string.title_commit));
    changedFilesTitleView.setText(getString(R.string.commit_changed_files, ""));
    final Repository repository = (Repository) getIntent().getSerializableExtra(EXTRA_REPOSITORY);
    final GitManager gitManager = gitManagerFactory.openRepository(repository);

    // load file content
    compositeSubscription.add(Observable.zip(
        gitManager.status(),
        gitManager.diff(),
        (status, diff) -> new Change(status, diff))
        .compose(new DefaultTransformer<Change>())
        .subscribe(change -> {
          Status status = change.status;

          // changed files, not including deleted files
          List<String> changedFiles = new ArrayList<>();
          changedFiles.addAll(status.getUncommittedChanges());
          changedFiles.addAll(status.getUntracked());
          changedFiles.addAll(status.getUntrackedFolders());
          changedFiles.removeAll(status.getMissing());

          // get deleted files and add 'deleted' flag
          List<String> deleteFiles = new ArrayList<>(status.getMissing());
          for (String deletedFile : deleteFiles) {
            changedFiles.add(getString(R.string.commit_deleted, deletedFile));
          }
          Collections.sort(changedFiles);

          // build final human readable change string
          StringBuilder builder = new StringBuilder();
          for (String file : changedFiles) {
            builder.append(file).append('\n');
          }
          changedFilesView.setText(builder.toString());
          changedFilesTitleView.setText(getString(R.string.commit_changed_files, String.valueOf(changedFiles.size())));

          // update diff
          diffView.setText(change.diff);

          // enable commit btn if changes are present
          commitButton.setEnabled(changedFiles.size() > 0);
        }, new ErrorActionBuilder()
            .add(new DefaultErrorAction(this, "failed to load git changes"))
            .build()));

    // setup commit button
    commitButton.setOnClickListener(v -> {
      showSpinner();
      String commitMessage = messageView.getText().toString();
      if ("".equals(commitMessage)) commitMessage = messageView.getHint().toString();

      compositeSubscription.add(gitManager.commitAllChanges(commitMessage)
          .compose(new DefaultTransformer<Void>())
          .subscribe(nothing -> {
            hideSpinner();
            Timber.d("commit success");
            setResult(RESULT_OK);
            Toast.makeText(CommitActivity.this, getString(R.string.commit_success), Toast.LENGTH_LONG).show();
            finish();
          }, new ErrorActionBuilder()
              .add(new DefaultErrorAction(CommitActivity.this, "failed to commit"))
              .add(new HideSpinnerAction(CommitActivity.this))
              .build()));
    });

    // setup expand buttons
    changedFilesLayout.setOnClickListener(v -> toggleExpand(changedFilesExpandButton, changedFilesView));
    diffLayout.setOnClickListener(v -> toggleExpand(diffExpandButton, diffView));
    messageLayout.setOnClickListener(v -> toggleExpand(messageExpandButton, messageView));

    // restore expansions
    if (savedInstanceState != null) {
      if (savedInstanceState.getBoolean(STATE_FILES_EXPAND)) {
        changedFilesView.setVisibility(View.VISIBLE);
        changedFilesExpandButton.setBackgroundResource(R.drawable.ic_expand_less);
      }
      if (savedInstanceState.getBoolean(STATE_DIFF_EXPAND)) {
        diffView.setVisibility(View.VISIBLE);
        diffExpandButton.setBackgroundResource(R.drawable.ic_expand_less);
      }
      if (savedInstanceState.getBoolean(STATE_MESSAGE_EXPAND)) {
        messageView.setVisibility(View.VISIBLE);
        messageExpandButton.setBackgroundResource(R.drawable.ic_expand_less);
      }
    }
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(STATE_FILES_EXPAND, changedFilesView.getVisibility() != View.GONE);
    outState.putBoolean(STATE_DIFF_EXPAND, diffView.getVisibility() != View.GONE);
    outState.putBoolean(STATE_MESSAGE_EXPAND, messageView.getVisibility() != View.GONE);
  }


  private void toggleExpand(ImageView button, View targetView) {
    if (targetView.getVisibility() == View.GONE) {
      expandView(targetView);
      button.setBackgroundResource(R.drawable.ic_expand_less);
    } else {
      collapseView(targetView);
      button.setBackgroundResource(R.drawable.ic_expand_more);
    }
  }


  /* Thanks to http://stackoverflow.com/a/13381228 */
  private void expandView(final View v) {
    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    final int targetHeight = v.getMeasuredHeight();

    v.getLayoutParams().height = 0;
    v.setVisibility(View.VISIBLE);
    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        v.getLayoutParams().height = interpolatedTime == 1
            ? LayoutParams.WRAP_CONTENT
            : (int) (targetHeight * interpolatedTime);
        v.requestLayout();
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }


  private void collapseView(final View v) {
    final int initialHeight = v.getMeasuredHeight();

    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (interpolatedTime == 1) {
          v.setVisibility(View.GONE);
        } else {
          v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
          v.requestLayout();
        }
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }


  private static class Change {

    private final Status status;
    private final String diff;

    public Change(Status status, String diff) {
      this.status = status;
      this.diff = diff;
    }

  }

}
