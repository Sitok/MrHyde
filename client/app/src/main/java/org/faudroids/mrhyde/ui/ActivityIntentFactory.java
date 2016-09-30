package org.faudroids.mrhyde.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.faudroids.mrhyde.git.Repository;

import java.io.File;

import javax.inject.Inject;

/**
 * Helper methods for creating intents to start the various activities.
 * Not static and part of individual activities as to not break the dependency injection chain.
 */
public class ActivityIntentFactory {

	private final Context context;

	@Inject
	ActivityIntentFactory(Context context) {
		this.context = context;
	}


  public Intent createRepoOverviewIntent(Repository repository) {
    Intent intent = new Intent(context, RepoOverviewActivity.class);
    intent.putExtra(RepoOverviewActivity.EXTRA_REPOSITORY, repository);
    return intent;
  }

	public Intent createPostsIntent(Repository repository) {
		Intent intent = new Intent(context, PostsActivity.class);
    intent.putExtra(PostsActivity.EXTRA_REPOSITORY, repository);
		return intent;
	}


	public Intent createDraftsIntent(Repository repository) {
		Intent intent = new Intent(context, DraftsActivity.class);
    intent.putExtra(DraftsActivity.EXTRA_REPOSITORY, repository);
		return intent;
	}


	public Intent createTextEditorIntent(Repository repository, File file, boolean isNewFile) {
		Intent intent = new Intent(context, TextEditorActivity.class);
		Bundle extras = createFileExtras(
				TextEditorActivity.EXTRA_REPOSITORY, repository,
				TextEditorActivity.EXTRA_FILE, file);
		extras.putBoolean(TextEditorActivity.EXTRA_IS_NEW_FILE, isNewFile);
		intent.putExtras(extras);
		return intent;
	}


	public Intent createImageViewerIntent(Repository repository, File file) {
		Intent intent = new Intent(context, ImageViewerActivity.class);
		intent.putExtras(createFileExtras(
				ImageViewerActivity.EXTRA_REPOSITORY, repository,
				ImageViewerActivity.EXTRA_FILE, file));
		return intent;
	}


	public Intent createPreviewIntent(Repository repository) {
		Intent previewIntent = new Intent(context, PreviewActivity.class);
		previewIntent.putExtra(PreviewActivity.EXTRA_REPO, repository);
		return previewIntent;
	}


	public Intent createCommitIntent(Repository repository) {
		Intent commitIntent = new Intent(context, CommitActivity.class);
		commitIntent.putExtra(CommitActivity.EXTRA_REPOSITORY, repository);
		return commitIntent;
	}


	private Bundle createFileExtras(
			String repositoryKey,
			Repository repository,
			String fileKey,
			File file) {

		Bundle extras = new Bundle();
		extras.putSerializable(repositoryKey, repository);
    extras.putSerializable(fileKey, file);
		return extras;
	}

}
