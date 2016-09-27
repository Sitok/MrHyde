package org.faudroids.mrhyde.ui.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.jekyll.AbstractJekyllContent;
import org.faudroids.mrhyde.jekyll.Draft;
import org.faudroids.mrhyde.jekyll.JekyllManager;
import org.faudroids.mrhyde.jekyll.Post;
import org.faudroids.mrhyde.ui.ActivityIntentFactory;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;

import java.io.File;
import java.text.DateFormat;

import javax.inject.Inject;

import rx.Observable;

/**
 * Jekyll specific UI utils.
 */
public class JekyllUiUtils {

  public interface OnContentCreatedListener<T> {

    void onContentCreated(T content);

  }


  private static final Typeface SANS_SERIF_LIGHT = Typeface.create("sans-serif-light", Typeface.NORMAL);
  private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance();

  private final Context context;
  private final ActivityIntentFactory intentFactory;

  @Inject
  JekyllUiUtils(Context context, ActivityIntentFactory intentFactory) {
    this.context = context;
    this.intentFactory = intentFactory;
  }


  public void setDraftOverview(View view, final Draft draft) {
    // set title
    TextView titleView = (TextView) view.findViewById(R.id.text_title);
    titleView.setText(draft.getTitle());
    titleView.setTypeface(SANS_SERIF_LIGHT);
  }


  public void setPostOverview(View view, final Post post) {
    // set title
    TextView titleView = (TextView) view.findViewById(R.id.text_title);
    titleView.setText(post.getTitle());
    titleView.setTypeface(SANS_SERIF_LIGHT);

    // set date
    TextView dateView = (TextView) view.findViewById(R.id.text_date);
    dateView.setText(DATE_FORMAT.format(post.getDate()));
  }


  /**
   * Displays a dialog for creating a new post file.
   */
  public void showNewPostDialog(
      final Activity activity,
      final JekyllManager jekyllManager,
      final Repository repository,
      final Optional<File> postsDir,
      final OnContentCreatedListener<Post> listener
  ) {
    showNewJekyllContentDialog(activity, new NewJekyllContentStrategy<Post>(R.string.new_post) {
      @Override
      public String formatTitle(String title) {
        return jekyllManager.postTitleToFilename(title);
      }

      @Override
      public Observable<Post> createNewItem(String title) {
        if (postsDir.isPresent()) return jekyllManager.createNewPost(title, postsDir.get());
        else return jekyllManager.createNewPost(title);
      }
    }, repository, listener);
  }


  /**
   * Displays a dialog for creating a new draft file.
   */
  public void showNewDraftDialog(
      final Activity activity,
      final JekyllManager jekyllManager,
      final Repository repository,
      final Optional<File> draftsDir,
      final OnContentCreatedListener<Draft> listener

  ) {
    showNewJekyllContentDialog(activity, new NewJekyllContentStrategy<Draft>(R.string.new_draft) {
      @Override
      public String formatTitle(String title) {
        return jekyllManager.draftTitleToFilename(title);
      }

      @Override
      public Observable<Draft> createNewItem(String title) {
        if (draftsDir.isPresent()) return jekyllManager.createNewDraft(title, draftsDir.get());
        else return jekyllManager.createNewDraft(title);
      }
    }, repository, listener);
  }


  private <T extends AbstractJekyllContent> void showNewJekyllContentDialog(
      final Activity activity,
      final NewJekyllContentStrategy<T> strategy,
      final Repository repository,
      final OnContentCreatedListener<T> listener
  ) {

    MaterialDialog dialog = new MaterialDialog.Builder(activity)
        .title(strategy.titleResource)
        .customView(R.layout.dialog_new_post_or_draft, false)
        .negativeText(android.R.string.cancel)
        .positiveText(android.R.string.ok)
        .onPositive((dialog1, which) -> {
          EditText titleView = (EditText) dialog1.getCustomView().findViewById(R.id.input);
          String newFileName = titleView.getText().toString();

          // open editor with new file
          strategy.createNewItem(newFileName)
              .compose(new DefaultTransformer<T>())
              .subscribe(item -> {
                File newFile = item.getFile();
                Intent newContentIntent = intentFactory.createTextEditorIntent(repository, newFile, true);
                activity.startActivity(newContentIntent);
                listener.onContentCreated(item);
              }, new ErrorActionBuilder()
                  .add(new DefaultErrorAction(context, "failed to create jekyll content"))
                  .build());
        })
        .build();

    View view = dialog.getCustomView();

    // update filename view when title changes
    final EditText titleView = (EditText) view.findViewById(R.id.input);
    final TextView fileNameView = (TextView) view.findViewById(R.id.text_filename);
    fileNameView.setText(strategy.formatTitle(context.getString(R.string.your_awesome_title)));
    titleView.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
      }

      @Override
      public void onTextChanged(CharSequence draftTitle, int start, int before, int count) {
        fileNameView.setText(strategy.formatTitle(draftTitle.toString()));
      }
    });

    // show dialog
    dialog.show();
  }


  private static abstract class NewJekyllContentStrategy<T extends AbstractJekyllContent> {

    private final int titleResource;

    public NewJekyllContentStrategy(int titleResource) {
      this.titleResource = titleResource;
    }

    public abstract String formatTitle(String title);

    public abstract Observable<T> createNewItem(String title);

  }

}
