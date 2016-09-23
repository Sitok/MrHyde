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
        .flatMap(previewData -> {
          Timber.d(previewData.getStaticFiles().get(0).getData());
          Timber.d("found " + previewData.getStaticFiles().size() + " binary files for preview");
          for (BinaryFile file : previewData.getStaticFiles()) Timber.d(file.getPath());
          return jekyllApi.createPreview(previewData);
        })
        .flatMap(previewResult -> Observable.just(previewResult.getPreviewUrl()));

  }

}
