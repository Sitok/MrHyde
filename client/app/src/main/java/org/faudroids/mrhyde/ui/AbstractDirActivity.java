package org.faudroids.mrhyde.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.FileUtils;
import org.faudroids.mrhyde.git.GitManager;
import org.faudroids.mrhyde.git.GitManagerFactory;
import org.faudroids.mrhyde.git.NodeUtils;
import org.faudroids.mrhyde.github.GitHubRepository;
import org.faudroids.mrhyde.jekyll.JekyllManager;
import org.faudroids.mrhyde.jekyll.JekyllManagerFactory;
import org.faudroids.mrhyde.ui.utils.AbstractActionBarActivity;
import org.faudroids.mrhyde.ui.utils.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows list of files in a repo and passes click events to sub classes.
 */
abstract class AbstractDirActivity extends AbstractActionBarActivity {

	static final String EXTRA_REPOSITORY = "EXTRA_REPOSITORY"; // which repo to show

	private final String STATE_SELECTED_DIR = "STATE_SELECTED_DIR"; // which dir is currently selected

	@BindView(R.id.list) protected RecyclerView recyclerView;
	protected FileAdapter fileAdapter;

  @Inject GitManagerFactory gitManagerFactory;
  protected GitManager gitManager;
	protected GitHubRepository repository;
	@Inject protected NodeUtils nodeUtils;
	@Inject protected FileUtils fileUtils;

	@Inject JekyllManagerFactory jekyllManagerFactory;
	protected JekyllManager jekyllManager;


	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    ButterKnife.bind(this);

		// get arguments
		repository = (GitHubRepository) this.getIntent().getSerializableExtra(EXTRA_REPOSITORY);
    gitManager = gitManagerFactory.openRepository(repository);
		jekyllManager = jekyllManagerFactory.createJekyllManager(gitManager);

		// setup list
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		fileAdapter = createAdapter();
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(fileAdapter);
		recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

		// get tree
		updateTree(savedInstanceState);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		fileAdapter.onSaveInstanceState(outState);
	}


	@Override
	public void onBackPressed() {
		// only go back when at the top dir
		if (!fileAdapter.onBackPressed()) {
			super.onBackPressed();
		}
	}


	/**
	 * Recreates the file tree
	 */
	protected void updateTree(final Bundle savedInstanceState) {
    invalidateOptionsMenu();
    if (savedInstanceState != null) {
      fileAdapter.onRestoreInstanceState(savedInstanceState);
    } else {
      fileAdapter.setSelectedDir(gitManager.getRootDir());
    }
  }


  protected FileAdapter createAdapter() {
    return new FileAdapter(gitManager.getRootDir());
  }


  protected abstract void onDirSelected(File file);

  protected abstract void onFileSelected(File file);


	protected class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private File rootDir;
    private File selectedDir;
		private final List<File> files = new ArrayList<>();


    public FileAdapter(File rootDir) {
      this.rootDir = rootDir;
    }

		@Override
		public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
			return new FileViewHolder(view);
		}


		@Override
		public void onBindViewHolder(FileViewHolder holder, int position) {
			holder.setFile(files.get(position));
		}


		@Override
		public int getItemCount() {
			return files.size();
		}


		public boolean onBackPressed() {
			// if no parent (or not loaded) let activity handle back press
      if (selectedDir.getParentFile() == null || selectedDir.getParentFile().equals(rootDir.getParentFile())) {
        return false;
      }
			// otherwise navigate up
			setSelectedDir(selectedDir.getParentFile());
			return true;
		}


		public void setSelectedDir(File newSelectedDir) {
      if (newSelectedDir.equals(rootDir)) setTitle(repository.getName());
			else setTitle(newSelectedDir.getName());
			selectedDir = newSelectedDir;
			files.clear();
      files.addAll(sortFiles(newSelectedDir.listFiles()));
			notifyDataSetChanged();
			onDirSelected(newSelectedDir);
		}


		public File getSelectedDir() {
			return selectedDir;
		}


		public void onSaveInstanceState(Bundle outState) {
      outState.putSerializable(STATE_SELECTED_DIR, selectedDir);
		}


		public void onRestoreInstanceState(Bundle inState) {
      File selectedDir =  (File) inState.getSerializable(STATE_SELECTED_DIR);
			if (selectedDir == null) return;
			setSelectedDir(selectedDir);
		}


		private List<File> sortFiles(File[] files) {
			List<File> dirs = new ArrayList<>();
			List<File> regularFiles = new ArrayList<>();
			for (File file: files) {
        if (file.isDirectory()) dirs.add(file);
        else regularFiles.add(file);
			}
			Collections.sort(dirs);
			Collections.sort(regularFiles);
			dirs.addAll(regularFiles);
			return dirs;
		}


		protected class FileViewHolder extends RecyclerView.ViewHolder {

			protected final View view;
			protected final TextView titleView;
			protected final ImageView iconView;

			public FileViewHolder(View view) {
				super(view);
				this.view = view;
				this.titleView = (TextView) view.findViewById(R.id.title);
				this.iconView = (ImageView) view.findViewById(R.id.icon);
			}

			public void setFile(final File file) {
				// setup node content
        titleView.setText(file.getName());

				int imageResource = R.drawable.folder;
        if (!file.isDirectory()) {
					if (fileUtils.isImage(file.getAbsolutePath())) {
						imageResource = R.drawable.image;
					} else if (jekyllManager.isPostsDirOrSubDir(file.getParentFile())
              && jekyllManager.parsePost(file).isPresent()) {
						imageResource = R.drawable.post;
					} else if (jekyllManager.isDraftsDirOrSubDir(file.getParentFile())
              && jekyllManager.parseDraft(file).isPresent()) {
						imageResource = R.drawable.draft;
					} else {
						imageResource = R.drawable.file;
					}
				}

				iconView.setImageResource(imageResource);

				view.setOnClickListener(v -> {
          if (file.isDirectory()) {
            // navigate "down"
            fileAdapter.setSelectedDir(file);
          } else {
            onFileSelected(file);
          }
        });
			}
		}
	}

}
