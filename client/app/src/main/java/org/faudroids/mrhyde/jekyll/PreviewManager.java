package org.faudroids.mrhyde.jekyll;


import org.faudroids.mrhyde.git.FileManager;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

public class PreviewManager {

	private final JekyllApi jekyllApi;
	private final RepoDetailsFactory repoDetailsFactory;

	@Inject
	PreviewManager(JekyllApi jekyllApi, RepoDetailsFactory repoDetailsFactory) {
		this.jekyllApi = jekyllApi;
		this.repoDetailsFactory = repoDetailsFactory;
	}


	/**
	 * Triggers a new preview and returns the preview URL.
	 */
	public Observable<String> loadPreview(FileManager fileManager) {
		Timber.d("starting new preview");
		return repoDetailsFactory.createRepoDetails(fileManager )
				.flatMap(repoDetails -> {
          Timber.d("found " + repoDetails.getStaticFiles().size() + " binary files for preview");
          for (BinaryFile file : repoDetails.getStaticFiles()) Timber.d(file.getPath());
          return jekyllApi.createPreview(repoDetails);
        })
				.flatMap(previewResult -> Observable.just(previewResult.getPreviewUrl()));

	}

}
