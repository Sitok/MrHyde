package org.faudroids.mrhyde.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.common.base.Optional;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.EmptyPermissionListener;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.ui.utils.ImageUtils;
import org.faudroids.mrhyde.ui.utils.JekyllUiUtils;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;

public final class DirActivity extends AbstractDirActivity implements DirActionModeListener.ActionSelectionListener {

  private static final String EXTRA_FILE_T0_MOVE = "EXTRA_FILE_TO_MOVE"; // marks which file should be moved

  private static final int
      REQUEST_COMMIT = 42,
      REQUEST_EDIT_FILE = 43,
      REQUEST_SELECT_PHOTO = 44,
      REQUEST_SELECT_DIR = 45;

  @BindView(R.id.tint) protected View tintView;
  @BindView(R.id.add) protected FloatingActionsMenu addButton;
  @BindView(R.id.add_file) protected FloatingActionButton addFileButton;
  @BindView(R.id.add_image) protected FloatingActionButton addImageButton;
  @BindView(R.id.add_folder) protected FloatingActionButton addFolderButton;
  @BindView(R.id.add_post) protected FloatingActionButton addPostButton;
  @BindView(R.id.add_draft) protected FloatingActionButton addDraftButton;

  @Inject ActivityIntentFactory intentFactory;
  @Inject ImageUtils imageUtils;

  @Inject JekyllUiUtils jekyllUiUtils;

  private GitActionBarMenu gitActionBarMenu;

  private DirActionModeListener actionModeListener = null;


  @Override
  public void onCreate(final Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    setContentView(R.layout.activity_dir);
    super.onCreate(savedInstanceState);
    setTitle(repository.getName());

    // setup add buttons
    addButton.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
      @Override
      public void onMenuExpanded() {
        tintView.animate().alpha(1).setDuration(200).start();
      }

      @Override
      public void onMenuCollapsed() {
        tintView.animate().alpha(0).setDuration(200).start();
      }
    });
    addFileButton.setOnClickListener(v -> {
      addButton.collapse();
      addAndOpenFile();
    });
    addImageButton.setOnClickListener(v -> {
      addButton.collapse();

      Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
      photoPickerIntent.setType("image/*");

      // if below v16 start immediately
      if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
        startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);

      } else {
        // assert has required permission
        Dexter.checkPermission(new EmptyPermissionListener() {
          @Override
          public void onPermissionGranted(PermissionGrantedResponse r) {
            startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
          }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
      }
    });
    addFolderButton.setOnClickListener(v -> {
      addButton.collapse();
      addDirectory();
    });
    addPostButton.setOnClickListener(v -> {
      addButton.collapse();
      jekyllUiUtils.showNewPostDialog(
          DirActivity.this,
          jekyllManager,
          repository,
          Optional.of(fileAdapter.getSelectedDir()),
          post -> refreshTree()
      );
    });
    addDraftButton.setOnClickListener(v -> {
      addButton.collapse();
      jekyllUiUtils.showNewDraftDialog(
          DirActivity.this,
          jekyllManager,
          repository,
          Optional.of(fileAdapter.getSelectedDir()),
          draft -> refreshTree()
      );
    });
    tintView.setOnClickListener(v -> addButton.collapse());

    // prepare action mode
    actionModeListener = new DirActionModeListener(this, this, uiUtils);

    // git related menu items
    gitActionBarMenu = new GitActionBarMenu(
        this,
        () -> {
         fileAdapter.setSelectedDir(gitManager.getRootDir());
          refreshTree();
        },
        gitManager,
        repository,
        intentFactory
    );
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_files, menu);
    return true;
  }


  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (isSpinnerVisible()) {
      menu.findItem(R.id.action_commit).setVisible(false);
      menu.findItem(R.id.action_preview).setVisible(false);
    }
    return super.onPrepareOptionsMenu(menu);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (gitActionBarMenu.onOptionsItemSelected(item)) return true;
    return super.onOptionsItemSelected(item);
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_COMMIT:
        if (resultCode != RESULT_OK) return;
      case REQUEST_EDIT_FILE:
        // refresh tree after successful commit or updated file (in case of new files)
        refreshTree();
        break;

      case REQUEST_SELECT_PHOTO:
        if (resultCode != RESULT_OK) return;
        final Uri selectedImage = data.getData();
        Timber.d(selectedImage.toString());

        // get name of new image file
        new MaterialDialog
            .Builder(this)
            .title(R.string.image_new_title)
            .content(R.string.image_new_message)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .alwaysCallInputCallback()
            .input(R.string.image_new_hint, 0, false, (dialog, input) -> {
              validateInputFileName(dialog, R.string.image_new_message, input.toString());
            })
            .onPositive((dialog, which) -> {
              String imageName = dialog.getInputEditText().getText().toString();
              // store image
              showSpinner();
              compositeSubscription.add(imageUtils
                  .loadAndSaveFile(selectedImage, new File(fileAdapter.getSelectedDir(), imageName))
                  .compose(new DefaultTransformer<>())
                  .subscribe(nothing -> {
                    hideSpinner();
                    refreshTree();
                  }, new ErrorActionBuilder()
                      .add(new DefaultErrorAction(DirActivity.this, "Failed to load and save image"))
                      .add(new HideSpinnerAction(DirActivity.this))
                      .build()));

            })
            .show();
        break;

      case REQUEST_SELECT_DIR:
        if (resultCode != RESULT_OK) return;

        // get selected dir and file
        final File selectedDir = (File) data.getSerializableExtra(SelectDirActivity.EXTRA_SELECTED_DIR);
        final File fileToMove = (File) data.getSerializableExtra(EXTRA_FILE_T0_MOVE);
        final File targetFile = new File(selectedDir, fileToMove.getName());

        // confirm overwriting files
        if (targetFile.exists()) {
          new MaterialDialog.Builder(this)
              .title(R.string.overwrite_file_title)
              .content(getString(R.string.overwrite_file_message, targetFile.getName()))
              .positiveText(R.string.overwrite_confirm)
              .negativeText(android.R.string.cancel)
              .onPositive((dialog, which) -> moveFile(fileToMove, targetFile))
              .show();
          return;
        }

        moveFile(fileToMove, targetFile);
        break;
    }
  }


  @Override
  public void onDelete(File file) {
    new MaterialDialog.Builder(this)
        .title(R.string.delete_title)
        .content(getString(R.string.delete_message, file.getName()))
        .positiveText(R.string.action_delete)
        .negativeText(android.R.string.cancel)
        .onPositive((dialog, which) -> compositeSubscription.add(
            fileUtils
                .deleteFile(file)
                .compose(new DefaultTransformer<>())
                .subscribe(
                    nothing -> refreshTree(),
                    new ErrorActionBuilder()
                        .add(new DefaultErrorAction(DirActivity.this, "Failed to delete file"))
                        .build()
                )
        ))
        .show();
  }


  @Override
  public void onEdit(File file) {
    startFileActivity(file, false);
  }


  @Override
  public void onRename(File file) {
    new MaterialDialog
        .Builder(this)
        .title(R.string.rename_title)
        .content(R.string.rename_message)
        .inputType(InputType.TYPE_CLASS_TEXT)
        .alwaysCallInputCallback()
        .input(getString(R.string.rename_hint), file.getName(), false, (dialog, input) -> {
          validateInputFileName(dialog, R.string.rename_message, input.toString());
        })
        .onPositive((dialog, which) -> {
          File targetFile = new File(
              fileAdapter.getSelectedDir(),
              dialog.getInputEditText().getText().toString()
          );
          compositeSubscription.add(fileUtils
              .renameFile(file, targetFile)
              .compose(new DefaultTransformer<>())
              .subscribe(
                  nothing -> refreshTree(),
                  new ErrorActionBuilder()
                      .add(new DefaultErrorAction(DirActivity.this, "Failed to rename file"))
                      .build()
              )
          );
        })
        .show();
  }


  @Override
  public void onMoveTo(File file) {
    Intent intent = new Intent(this, SelectDirActivity.class);
    intent.putExtra(SelectDirActivity.EXTRA_REPOSITORY, repository);
    intent.putExtra(EXTRA_FILE_T0_MOVE, file);
    for (String key : intent.getExtras().keySet()) Timber.d("found key " + key);
    startActivityForResult(intent, REQUEST_SELECT_DIR);
  }


  private void moveFile(File fileToMove, File targetFile) {
    compositeSubscription.add(fileUtils
        .renameFile(fileToMove, targetFile, true)
        .compose(new DefaultTransformer<>())
        .subscribe(
            nothing -> {
              Toast.makeText(DirActivity.this, getString(R.string.file_moved), Toast.LENGTH_SHORT).show();
              refreshTree();
            }, new ErrorActionBuilder()
                .add(new DefaultErrorAction(DirActivity.this, "Failed to move file"))
                .build()
        )
    );
  }


  @Override
  public void onStopActionMode() {
    fileAdapter.notifyDataSetChanged();
  }


  private void addAndOpenFile() {
    new MaterialDialog
        .Builder(this)
        .title(R.string.file_new_title)
        .content(R.string.file_new_message)
        .inputType(InputType.TYPE_CLASS_TEXT)
        .alwaysCallInputCallback()
        .input(R.string.file_new_hint, 0, false, (dialog, input) -> {
          validateInputFileName(dialog, R.string.file_new_message, input.toString());
        })
        .onPositive((dialog, which) -> {
          // create file
          String newFileName = dialog.getInputEditText().getText().toString();
          File file = new File(fileAdapter.getSelectedDir(), newFileName);
          fileUtils
              .createNewFile(file)
              .subscribe(
                  nothing -> startFileActivity(file, true),
                  new ErrorActionBuilder()
                      .add(new DefaultErrorAction(DirActivity.this, "Failed to create file"))
                      .build()
              );
        })
        .show();
  }


  private void addDirectory() {
    new MaterialDialog
        .Builder(this)
        .title(R.string.dir_new_title)
        .content(R.string.dir_new_message)
        .inputType(InputType.TYPE_CLASS_TEXT)
        .alwaysCallInputCallback()
        .input(R.string.dir_new_hint, 0, false, (dialog, input) -> {
          validateInputFileName(dialog, R.string.dir_new_message, input.toString());
        })
        .onPositive((dialog, which) -> {
          // create directory
          String newDirName = dialog.getInputEditText().getText().toString();
          File directory = new File(fileAdapter.getSelectedDir(), newDirName);
          fileUtils
              .createNewDirectory(directory)
              .subscribe(
                  nothing -> refreshTree(),
                  new ErrorActionBuilder()
                      .add(new DefaultErrorAction(DirActivity.this, "Failed to create directory"))
                      .build()
              );
        })
        .show();
  }

  private boolean validateInputFileName(MaterialDialog dialog, @StringRes int content, String fileName) {
    boolean isValid = true;

    // check for empty file name
    if (fileName.length() == 0) {
      isValid = false;
    }

    // check if file already exists
    if (isValid) {
      for (File file : fileAdapter.getSelectedDir().listFiles()) {
        if (file.getName().equals(fileName)) {
          isValid = false;
          content = R.string.file_already_exists;
          break;
        }
      }
    }

    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(isValid);
    dialog.setContent(content);
    return isValid;
  }


  /**
   * Recreates the file tree without changing directory
   */
  private void refreshTree() {
    Bundle tmpSavedState = new Bundle();
    fileAdapter.onSaveInstanceState(tmpSavedState);
    updateTree(tmpSavedState);
  }


  private void startFileActivity(File file, boolean isNewFile) {
    actionModeListener.stopActionMode();

    // start text or image activity depending on file name
    if (!fileUtils.isImage(file.getName())) {
      // start text editor
      Intent editorIntent = intentFactory.createTextEditorIntent(repository, file, isNewFile);
      startActivityForResult(editorIntent, REQUEST_EDIT_FILE);

    } else {
      // start image viewer
      Intent viewerIntent = intentFactory.createImageViewerIntent(repository, file);
      startActivity(viewerIntent);
    }

  }


  @Override
  protected FileAdapter createAdapter() {
    return new LongClickFileAdapter(gitManager.getRootDir());
  }


  @Override
  protected void onDirSelected(File directory) {
    // show / hide posts add button
    if (jekyllManager.isPostsDirOrSubDir(directory)) addPostButton.setVisibility(View.VISIBLE);
    else addPostButton.setVisibility(View.GONE);

    // show / hide drafts add button
    if (jekyllManager.isDraftsDirOrSubDir(directory)) addDraftButton.setVisibility(View.VISIBLE);
    else addDraftButton.setVisibility(View.GONE);
  }


  @Override
  protected void onFileSelected(File file) {
    startFileActivity(file, false);
  }


  @Override
  public void onBackPressed() {
    // hide open add buttons
    if (addButton.isExpanded()) addButton.collapse();
    else super.onBackPressed();
  }


  public class LongClickFileAdapter extends FileAdapter {

    public LongClickFileAdapter(File rootDir) {
      super(rootDir);
    }

    @Override
    public LongClickFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
      return new LongClickFileViewHolder(view);
    }

    public class LongClickFileViewHolder extends FileViewHolder {

      public LongClickFileViewHolder(View view) {
        super(view);
      }

      @Override
      public void setFile(File file) {
        super.setFile(file);

        // check for action mode
        File selectedFile = actionModeListener.getSelectedFile();
        if (selectedFile != null && selectedFile.equals(file)) {
          view.setSelected(true);
        } else {
          view.setSelected(false);
        }

        // setup long click
        if (!file.isDirectory()) {
          view.setOnLongClickListener(v -> {
            // only highlight item when selection was successful
            if (actionModeListener.startActionMode(file)) {
              view.setSelected(true);
            }
            return true;
          });
        } else {
          view.setLongClickable(false);
        }
      }
    }
  }

}
