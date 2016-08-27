package org.faudroids.mrhyde.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.ortiz.touch.TouchImageView;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.FileData;
import org.faudroids.mrhyde.git.FileManager;
import org.faudroids.mrhyde.git.FileManagerFactory;
import org.faudroids.mrhyde.git.FileNode;
import org.faudroids.mrhyde.git.NodeUtils;
import org.faudroids.mrhyde.github.GitHubRepository;
import org.faudroids.mrhyde.ui.utils.AbstractActionBarActivity;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ImageViewerActivity extends AbstractActionBarActivity {

	static final String
			EXTRA_REPOSITORY = "EXTRA_REPOSITORY",
			EXTRA_FILE_NODE = "EXTRA_FILE_NODE";

	private static final String STATE_CONTENT = "STATE_CONTENT";

	@BindView(R.id.image) protected TouchImageView imageView;

	@Inject FileManagerFactory fileManagerFactory;
	@Inject NodeUtils nodeUtils;
	private FileManager fileManager;
	private FileData fileData; // image currently being viewed


	@Override
	public void onCreate(final Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
		super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_viewer);
    ButterKnife.bind(this);

		// load arguments
		final GitHubRepository repository = (GitHubRepository) getIntent().getSerializableExtra(EXTRA_REPOSITORY);
		fileManager = fileManagerFactory.createFileManager(repository);

		// load image
		if (savedInstanceState != null && savedInstanceState.getSerializable(STATE_CONTENT) != null) {
			fileData = (FileData) savedInstanceState.getSerializable(STATE_CONTENT);
			setupImage();

		} else {
			showSpinner();
			compositeSubscription.add(fileManager.getTree()
					.flatMap(rootNode -> {
            FileNode node = (FileNode) nodeUtils.restoreNode(EXTRA_FILE_NODE, getIntent(), rootNode);
            return fileManager.readFile(node);
          })
					.compose(new DefaultTransformer<FileData>())
					.subscribe(file -> {
            hideSpinner();
            ImageViewerActivity.this.fileData = file;
            setupImage();
          }, new ErrorActionBuilder()
							.add(new DefaultErrorAction(this, "failed to get image content"))
							.add(new HideSpinnerAction(this))
							.build()));
		}
	}


	private void setupImage() {
		setTitle(fileData.getFileNode().getPath());
		Bitmap bitmap = BitmapFactory.decodeByteArray(fileData.getData(), 0, fileData.getData().length);
		imageView.setImageBitmap(bitmap);
		imageView.setZoom(0.999999f); // hack, if not present image is sometimes empty
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(STATE_CONTENT, fileData);
	}

}
