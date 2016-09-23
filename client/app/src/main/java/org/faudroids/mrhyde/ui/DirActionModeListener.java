package org.faudroids.mrhyde.ui;

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.faudroids.mrhyde.R;

import java.io.File;

class DirActionModeListener implements ActionMode.Callback {

  private final Activity activity;
  private final ActionSelectionListener selectionListener;

  private File selectedFile = null;
  private ActionMode actionMode;


  public DirActionModeListener(Activity activity, ActionSelectionListener selectionListener) {
    this.activity = activity;
    this.selectionListener = selectionListener;
  }


  public boolean startActionMode(File selectedFile) {
    if (this.selectedFile != null) return false;
    this.selectedFile = selectedFile;
    this.actionMode = this.activity.startActionMode(this);
    return true;
  }


  public void stopActionMode() {
    if (actionMode != null) actionMode.finish();
  }


  public File getSelectedFile() {
    return selectedFile;
  }


  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuInflater inflater = mode.getMenuInflater();
    if (selectedFile.isDirectory()) inflater.inflate(R.menu.menu_files_action_mode_dir, menu);
    else inflater.inflate(R.menu.menu_files_action_mode_file, menu);
    return true;
  }


  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }


  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_edit:
        selectionListener.onEdit(selectedFile);
        stopActionMode();
        return true;

      case R.id.action_delete:
        selectionListener.onDelete(selectedFile);
        stopActionMode();
        return true;

      case R.id.action_rename:
        selectionListener.onRename(selectedFile);
        stopActionMode();
        return true;

      case R.id.action_move:
        selectionListener.onMoveTo(selectedFile);
        stopActionMode();
        return true;
    }
    return false;
  }


  @Override
  public void onDestroyActionMode(ActionMode mode) {
    this.selectedFile = null;
    this.actionMode = null;
    selectionListener.onStopActionMode();
  }


  public interface ActionSelectionListener {

    void onDelete(File file);
    void onEdit(File file);
    void onRename(File file);
    void onMoveTo(File file);
    void onStopActionMode();

  }

}
