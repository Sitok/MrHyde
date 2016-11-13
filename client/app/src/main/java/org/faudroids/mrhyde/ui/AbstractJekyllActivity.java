package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.AddFloatingActionButton;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.FileUtils;
import org.faudroids.mrhyde.git.RepositoriesManager;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.jekyll.AbstractJekyllContent;
import org.faudroids.mrhyde.jekyll.JekyllManager;
import org.faudroids.mrhyde.jekyll.JekyllManagerFactory;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.ui.utils.DividerItemDecoration;
import org.faudroids.mrhyde.ui.utils.JekyllUiUtils;
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

abstract class AbstractJekyllActivity<T extends AbstractJekyllContent & Comparable<T>>
    extends AbstractActivity
    implements JekyllActionModeListener.ActionSelectionListener<T> {

  private static final int REQUEST_COMMIT = 42;

  static final String EXTRA_REPOSITORY = "EXTRA_REPOSITORY";


  @BindView(R.id.list) protected RecyclerView recyclerView;
  protected AbstractAdapter adapter;
  @Inject protected JekyllUiUtils jekyllUiUtils;

  @BindView(R.id.empty) protected TextView emptyView;
  @BindView(R.id.add) protected AddFloatingActionButton addButton;

  protected Repository repository;
  @Inject RepositoriesManager repositoriesManager;
  @Inject JekyllManagerFactory jekyllManagerFactory;
  protected JekyllManager jekyllManager;

  @Inject protected FileUtils fileUtils;

  @Inject ActivityIntentFactory intentFactory;

  private JekyllActionModeListener<T> actionModeListener;

  private final int
      titleStringResource,
      emptyStringResource,
      moveActionStringResource,
      movedConfirmationStringResource,
      moveTitleStringResource,
      moveMessageStringResource;


  AbstractJekyllActivity(
      int titleStringResource,
      int emptyStringResource,
      int moveActionStringResource,
      int movedConfirmationStringResource,
      int moveTitleStringResource,
      int moveMessageStringResource) {

    this.titleStringResource = titleStringResource;
    this.emptyStringResource = emptyStringResource;
    this.moveActionStringResource = moveActionStringResource;
    this.movedConfirmationStringResource = movedConfirmationStringResource;
    this.moveTitleStringResource = moveTitleStringResource;
    this.moveMessageStringResource = moveMessageStringResource;
  }


  protected abstract void onAddClicked(JekyllUiUtils.OnContentCreatedListener<T> contentListener);

  protected abstract Observable<List<T>> doLoadItems();

  protected abstract AbstractAdapter createAdapter();

  protected abstract Observable<?> createMoveObservable(T item);

  protected abstract String getMovedFilenameForItem(T item); // what the item would be called if it were move to the other folder


  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_posts_or_drafts);
    ButterKnife.bind(this);

    // get arguments
    repository = (Repository) getIntent().getSerializableExtra(EXTRA_REPOSITORY);
    jekyllManager = jekyllManagerFactory.createJekyllManager(
        repositoriesManager.openRepository(repository)
    );

    // set title
    setTitle(getString(titleStringResource));

    // setup list
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    adapter = createAdapter();
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

    // setup add
    addButton.setOnClickListener(v -> {
      actionModeListener.stopActionMode();
      onAddClicked(newItem -> adapter.addItem(newItem));
    });

    // prepare action mode
    actionModeListener = new JekyllActionModeListener<>(this, this, moveActionStringResource);

    // load posts
    loadItems();
  }


  private void loadItems() {
    compositeSubscription.add(doLoadItems()
        .compose(new DefaultTransformer<List<T>>())
        .subscribe(items -> {
          if (isSpinnerVisible()) hideSpinner();
          adapter.setItems(items);
          if (items.isEmpty()) {
            emptyView.setText(getString(emptyStringResource));
            emptyView.setVisibility(View.VISIBLE);
          } else {
            emptyView.setVisibility(View.GONE);
          }
        }, new ErrorActionBuilder()
            .add(new DefaultErrorAction(AbstractJekyllActivity.this, "failed to load content"))
            .add(new HideSpinnerAction(AbstractJekyllActivity.this))
            .build()));
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_posts, menu);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_commit:
        startActivityForResult(intentFactory.createCommitIntent(repository), REQUEST_COMMIT);
        return true;

      case R.id.action_preview:
        startActivity(intentFactory.createPreviewIntent(repository));
        return true;

    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  public void onActivityResult(int request, int result, Intent data) {
    switch (request) {
      case REQUEST_COMMIT:
        if (result != RESULT_OK) return;
        showSpinner();
        loadItems();
    }
  }


  @Override
  public void onShare(T item) {
    fileUtils.shareRepositoryFile(item.getFile());
  }


  @Override
  public void onDelete(final T item) {
    new MaterialDialog.Builder(this)
        .title(R.string.delete_file_title)
        .content(getString(R.string.delete_message, item.getFile().getName()))
        .positiveText(R.string.action_delete)

        .onPositive((dialog, which) -> compositeSubscription.add(
            fileUtils
                .deleteFile(item.getFile())
                .compose(new DefaultTransformer<>())
                .subscribe(
                    nothing -> loadItems(),
                    new ErrorActionBuilder()
                        .add(new DefaultErrorAction(this, "Failed to delete file"))
                        .build()
                )
        ))
        .negativeText(android.R.string.cancel)
        .show();
  }


  @Override
  public void onEdit(T item) {
    startActivity(intentFactory.createTextEditorIntent(repository, item.getFile(), false));
  }


  @Override
  public void onMove(final T item) {
    new MaterialDialog.Builder(this)
        .title(moveTitleStringResource)
        .content(getString(moveMessageStringResource, getMovedFilenameForItem(item)))
        .positiveText(R.string.move)
        .onPositive((dialog, which) -> createMoveObservable(item)
            .compose(new DefaultTransformer<>())
            .subscribe(o -> {
              adapter.removeItem(item);
              Toast.makeText(AbstractJekyllActivity.this, getString(movedConfirmationStringResource), Toast.LENGTH_SHORT).show();
            }, new ErrorActionBuilder()
                .add(new DefaultErrorAction(AbstractJekyllActivity.this, "failed to move content"))
                .build()))
        .negativeText(android.R.string.cancel)
        .show();
  }


  @Override
  public void onStopActionMode() {
    adapter.notifyDataSetChanged();
  }


  abstract class AbstractAdapter extends RecyclerView.Adapter<AbstractAdapter.AbstractViewHolder> {

    private final List<T> itemsList = new ArrayList<>();

    @Override
    public void onBindViewHolder(AbstractViewHolder holder, int position) {
      holder.setItem(itemsList.get(position));
    }

    @Override
    public int getItemCount() {
      return itemsList.size();
    }

    public void setItems(List<T> itemsList) {
      this.itemsList.clear();
      this.itemsList.addAll(itemsList);
      notifyDataSetChanged();
    }

    public void addItem(T item) {
      itemsList.add(item);
      Collections.sort(itemsList);
      notifyDataSetChanged();
    }

    public void removeItem(T item) {
      itemsList.remove(item);
      notifyDataSetChanged();
    }


    abstract class AbstractViewHolder extends RecyclerView.ViewHolder {

      protected final View view;

      public AbstractViewHolder(View view) {
        super(view);
        this.view = view;
      }

      public void setItem(final T item) {
        // set on click
        view.setOnClickListener(v -> {
          actionModeListener.stopActionMode();
          startActivity(intentFactory.createTextEditorIntent(repository, item.getFile(), false));
        });

        // set long click starts action mode
        view.setOnLongClickListener(v -> {
          if (actionModeListener.startActionMode(item)) {
            v.setSelected(true);
          }
          return true;
        });

        // check if item is selected
        if (item.equals(actionModeListener.getSelectedItem())) {
          view.setSelected(true);
        } else {
          view.setSelected(false);
        }

        doSetItem(item);
      }

      protected abstract void doSetItem(T item);
    }
  }

}
