package org.faudroids.mrhyde.jekyll;


import org.faudroids.mrhyde.git.GitManager;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

public class PreviewManager {

  private final JekyllApi jekyllApi;
  private final PreviewRequestDataFactory previewRequestDataFactory;

  @Inject
  PreviewManager(JekyllApi jekyllApi, PreviewRequestDataFactory previewRequestDataFactory) {
    this.jekyllApi = jekyllApi;
    this.previewRequestDataFactory = previewRequestDataFactory;
  }


  /**
   * Triggers a new preview and returns the preview URL.
   */
  public Observable<String> loadPreview(GitManager gitManager) {
    Timber.d("starting new preview");
    return previewRequestDataFactory.createRepoDetails(gitManager)
        .flatMap(repoDetails -> {
          Timber.d("found " + repoDetails.getStaticFiles().size() + " binary files for preview");
          for (BinaryFile file : repoDetails.getStaticFiles()) Timber.d(file.getPath());
          return jekyllApi.createPreview(repoDetails);
        })
        .flatMap(previewResult -> Observable.just(previewResult.getPreviewUrl()));

  }

}
