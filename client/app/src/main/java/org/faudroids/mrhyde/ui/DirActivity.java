package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.ui.utils.ImageUtils;
import org.faudroids.mrhyde.ui.utils.JekyllUiUtils;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;

public final class DirActivity extends AbstractDirActivity implements DirActionModeListener.ActionSelectionListener {

	private static final String EXTRA_NODE_TO_MOVE = "EXTRA_NODE_TO_MOVE"; // marks which file should be moved

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
      startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
    });
		addFolderButton.setOnClickListener(v -> {
      addButton.collapse();
      addDirectory();
    });
		addPostButton.setOnClickListener(v -> {
      /*)
      addButton.collapse();
      jekyllUiUtils.showNewPostDialog(
          jekyllManager,
          repository,
          Optional.of(fileAdapter.getSelectedDir()),
          content -> refreshTree());
          */
    });
		addDraftButton.setOnClickListener(v -> {
      // TODO
      /*
      addButton.collapse();
      jekyllUiUtils.showNewDraftDialog(
          jekyllManager,
          repository,
          Optional.of(fileAdapter.getSelectedDir()),
          new JekyllUiUtils.OnContentCreatedListener<Draft>() {
        @Override
        public void onContentCreated(Draft content) {
          refreshTree();
        }
      });
      */
    });
		tintView.setOnClickListener(v -> addButton.collapse());

		// prepare action mode
		actionModeListener = new DirActionModeListener(this, this, uiUtils);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.files, menu);
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isSpinnerVisible()) {
			menu.findItem(R.id.action_commit).setVisible(false);
			menu.findItem(R.id.action_preview).setVisible(false);
			menu.findItem(R.id.action_discard_changes).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
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

			case R.id.action_discard_changes:
				new AlertDialog.Builder(this)
						.setTitle(R.string.delete_repo_title)
						.setMessage(R.string.delete_repo_message)
						.setPositiveButton(android.R.string.ok, (dialog, which) -> {
              fileManager.resetRepository();
              updateTree(null);
            })
						.setNegativeButton(android.R.string.cancel, null)
						.show();
				return true;
		}
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
        // TODO
        /*
				// get image name
				uiUtils.createInputDialog(
						R.string.image_new_title,
						R.string.image_new_message,
            imageName -> {
              // store image
              FileNode imageNode = fileManager.createNewFile(fileAdapter.getSelectedDir(), imageName);
              showSpinner();
              compositeSubscription.add(imageUtils.loadImage(imageNode, selectedImage)
                  .compose(new DefaultTransformer<FileData>())
                  .subscribe(data1 -> {
                    hideSpinner();
                    try {
                      fileManager.writeFile(data1);
                      refreshTree();
                    } catch (IOException ioe) {
                      Timber.e(ioe, "failed to write file");
                    }
                  }, new ErrorActionBuilder()
                      .add(new DefaultErrorAction(DirActivity.this, "failed to write file"))
                      .add(new HideSpinnerAction(DirActivity.this))
                      .build()));
            })
						.show();
						*/
				break;

			case REQUEST_SELECT_DIR:
				if (resultCode != RESULT_OK) return;

        // TODO
        /*
				// get root note
				DirNode rootNode = fileAdapter.getSelectedDir();
				while (rootNode.getParent() != null) rootNode = rootNode.getParent();

				// get selected dir and file
				final DirNode selectedDir = (DirNode) nodeUtils.restoreNode(SelectDirActivity.EXTRA_SELECTED_DIR, data, rootNode);
				final FileNode selectedFile = (FileNode) nodeUtils.restoreNode(EXTRA_NODE_TO_MOVE, data, rootNode);

				// confirm overwriting files
				if (selectedDir.getEntries().containsKey(selectedFile.getPath())) {
					new AlertDialog.Builder(this)
							.setTitle(getString(R.string.overwrite_file_title))
							.setMessage(getString(R.string.overwrite_file_message, selectedFile.getPath()))
							.setPositiveButton(getString(R.string.overwrite_confirm), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									moveFile(selectedFile, selectedDir);
								}
							})
							.setNegativeButton(android.R.string.cancel, null)
							.show();
				} else {
					moveFile(selectedFile, selectedDir);
				}
				*/

				break;
		}
	}


	@Override
	public void onDelete(final File file) {
    // TODO
    /*
		new AlertDialog.Builder(this)
				.setTitle(R.string.delete_title)
				.setMessage(getString(R.string.delete_message, fileNode.getPath()))
				.setPositiveButton(getString(R.string.action_delete), (dialog, which) -> {
          showSpinner();
          compositeSubscription.add(fileManager.deleteFile(fileNode)
              .compose(new DefaultTransformer<Void>())
              .subscribe(aVoid -> {
                hideSpinner();
                updateTree(null);
              }, new ErrorActionBuilder()
                  .add(new DefaultErrorAction(DirActivity.this, "failed to delete file"))
                  .add(new HideSpinnerAction(DirActivity.this))
                  .build()));
        })
				.setNegativeButton(android.R.string.cancel, null)
				.show();
				*/
	}


	@Override
	public void onEdit(File file) {
		startFileActivity(file, false);
	}


	@Override
	public void onRename(File file, String newFileName) {
    // TODO
    /*
		showSpinner();
		compositeSubscription.add(fileManager.renameFile(fileNode, newFileName)
				.compose(new DefaultTransformer<FileNode>())
				.subscribe(newFileNode -> {
          hideSpinner();
          updateTree(null);
        }, new ErrorActionBuilder()
						.add(new DefaultErrorAction(this, "failed to rename file"))
						.add(new HideSpinnerAction(this))
						.build()));
						*/
	}


	@Override
	public void onMoveTo(File file) {
		Intent intent = new Intent(this, SelectDirActivity.class);
		intent.putExtra(SelectDirActivity.EXTRA_REPOSITORY, repository);
    intent.putExtra(EXTRA_NODE_TO_MOVE, file);
		for (String key : intent.getExtras().keySet()) Timber.d("found key " + key);
		startActivityForResult(intent, REQUEST_SELECT_DIR);
	}


	private void moveFile(File fileToMove, File targetDir) {
    // TODO
    /*
		showSpinner();
		compositeSubscription.add(fileManager.moveFile(fileToMove, targetDir)
				.compose(new DefaultTransformer<FileNode>())
				.subscribe(newFileNode -> {
          hideSpinner();
          Toast.makeText(DirActivity.this, getString(R.string.file_moved), Toast.LENGTH_SHORT).show();
          refreshTree();
        }, new ErrorActionBuilder()
						.add(new DefaultErrorAction(DirActivity.this, "failed to move file"))
						.add(new HideSpinnerAction(DirActivity.this))
						.build()));
						*/
	}


	@Override
	public void onStopActionMode() {
		fileAdapter.notifyDataSetChanged();
	}


	private void addAndOpenFile() {
		uiUtils.createInputDialog(
        this,
				R.string.file_new_title,
				R.string.file_new_message,
        input -> {
          File file = new File(fileAdapter.getSelectedDir(), input);
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
    uiUtils.createInputDialog(
        this,
				R.string.dir_new_title,
				R.string.dir_new_message,
        input -> {
          File directory = new File(fileAdapter.getSelectedDir(), input);
          fileUtils
              .createNewDirectory(directory)
              .subscribe(
                  nothing -> {
                    Bundle state = new Bundle();
                    fileAdapter.onSaveInstanceState(state);
                    updateTree(state);
                  },
                  new ErrorActionBuilder()
                      .add(new DefaultErrorAction(DirActivity.this, "Failed to create directory"))
                      .build()
              );

        })
				.show();
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

        // TODO
        /*
				// check for action mode
				if (actionModeListener.getSelectedNode() != null && actionModeListener.getSelectedNode().equals(pathNode)) {
					view.setSelected(true);
				} else {
					view.setSelected(false);
				}

				// setup long click
				if (pathNode instanceof FileNode) {
					view.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							// only highlight item when selection was successful
							if (actionModeListener.startActionMode((FileNode) pathNode)) {
								view.setSelected(true);
							}
							return true;
						}
					});
				} else {
					view.setLongClickable(false);
				}
				*/
			}
		}
	}

}
