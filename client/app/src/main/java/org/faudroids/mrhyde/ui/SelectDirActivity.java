package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.AbstractNode;
import org.faudroids.mrhyde.git.DirNode;
import org.faudroids.mrhyde.git.FileNode;

import butterknife.BindView;
import timber.log.Timber;

public final class SelectDirActivity extends AbstractDirActivity {

	static final String EXTRA_SELECTED_DIR = "EXTRA_SELECTED_DIR";	// part of result of this activity

	@BindView(R.id.back) protected View backView;
	@BindView(R.id.cancel) protected View cancelView;
	@BindView(R.id.confirm) protected View confirmView;


	@Override
	public void onCreate(final Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    setContentView(R.layout.dialog_select_dir);
		super.onCreate(savedInstanceState);

		// hide action bar
		if (getSupportActionBar() != null) getSupportActionBar().hide();

		// setup buttons
		backView.setOnClickListener(v -> finish());
		cancelView.setOnClickListener(v -> finish());
		confirmView.setOnClickListener(v -> {
      // return result
      for (String key : getIntent().getExtras().keySet()) Timber.d("found key " + key);
      Intent resultIntent = new Intent(getIntent());
      for (String key : resultIntent.getExtras().keySet()) Timber.d("found key " + key);
      nodeUtils.saveNode(EXTRA_SELECTED_DIR, resultIntent, pathNodeAdapter.getSelectedNode());
      for (String key : getIntent().getExtras().keySet()) Timber.d("found key " + key);
      setResult(RESULT_OK, resultIntent);
      finish();
    });
	}


	@Override
	protected PathNodeAdapter createAdapter() {
		return new AlphaPathNodeAdapter();
	}


	@Override
	protected void onDirSelected(DirNode node) {
		// nothing to do
	}


	@Override
	protected void onFileSelected(FileNode node) {
		// nothing to do
	}


	public class AlphaPathNodeAdapter extends PathNodeAdapter {

		@Override
		public AlphaPathNodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
			return new AlphaPathNodeViewHolder(view);
		}


		public class AlphaPathNodeViewHolder extends PathNodeAdapter.PathNodeViewHolder {

			public AlphaPathNodeViewHolder(View view) {
				super(view);
			}

			@Override
			public void setViewForNode(final AbstractNode pathNode) {
				super.setViewForNode(pathNode);

				// remove left + right margin due to dialog container
				ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
				params.leftMargin = 0;
				params.rightMargin = 0;
				view.setLayoutParams(params);

				// reduce alpha for files
				float alpha = (pathNode instanceof DirNode) ? 1f : 0.3f;
				iconView.setAlpha(alpha);
				titleView.setAlpha(alpha);
			}
		}
	}

}
