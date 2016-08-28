package org.faudroids.mrhyde.ui;

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.ui.utils.UiUtils;

import java.io.File;

class DirActionModeListener implements ActionMode.Callback {

	private final Activity activity;
	private final ActionSelectionListener selectionListener;
	private final UiUtils uiUtils;

	private File selectedFile = null;
	private ActionMode actionMode;


	public DirActionModeListener(Activity activity, ActionSelectionListener selectionListener, UiUtils uiUtils) {
		this.activity = activity;
		this.selectionListener = selectionListener;
		this.uiUtils = uiUtils;
	}


	public boolean startActionMode(File selectedFile) {
		if (this.selectedFile != null) return false;
		this.actionMode = this.activity.startActionMode(this);
		this.selectedFile = selectedFile;
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
		inflater.inflate(R.menu.files_action_mode, menu);
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
        // TODO
        /*
				uiUtils.createInputDialog(activity, activity.getString(R.string.rename_title), selectedFile.getPath(), newFileName -> {
          selectionListener.onRename(selectedFile, newFileName);
          stopActionMode();
        }).show();
        */
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
		void onRename(File file, String newFileName);
		void onMoveTo(File file);
		void onStopActionMode();

	}

}
