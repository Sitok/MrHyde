package org.faudroids.mrhyde.jekyll;


import android.content.Context;
import android.util.Base64;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.FileUtils;
import org.faudroids.mrhyde.git.GitManager;
import org.faudroids.mrhyde.github.LoginManager;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

public class PreviewManager {

  private final JekyllApi jekyllApi;
  private final String clientSecret;
  private final LoginManager loginManager;
  private final FileUtils fileUtils;

  @Inject
  PreviewManager(
      Context context,
      JekyllApi jekyllApi,
      LoginManager loginManager,
      FileUtils fileUtils) {

    this.jekyllApi = jekyllApi;
    this.clientSecret = context.getString(R.string.jekyllServerClientSecret);
    this.loginManager = loginManager;
    this.fileUtils = fileUtils;
  }


  /**
   * Triggers a new preview and returns the preview URL.
   */
  public Observable<String> loadPreview(GitManager gitManager) {
    Timber.d("starting new preview");
    return createRepoDetails(gitManager)
        .flatMap(previewData -> {
          Timber.d("found " + previewData.getStaticFiles().size() + " binary files for preview");
          for (BinaryFile file : previewData.getStaticFiles()) Timber.d(file.getPath());
          return jekyllApi.createPreview(previewData);
        })
        .flatMap(previewResult -> Observable.just(previewResult.getPreviewUrl()));

  }

  private Observable<PreviewRequestData> createRepoDetails(GitManager gitManager) {
    return getChangedBinaryFiles(gitManager)
        .flatMap(binaryFileNames -> gitManager
            .diff(binaryFileNames)
            .flatMap(nonBinaryDiff -> {
              Timber.d("non binary diff is " + nonBinaryDiff);
              return Observable
                  .from(binaryFileNames)
                  .flatMap(binaryFileName -> {
                    Timber.d("reading binary file " + binaryFileName);
                    return fileUtils
                        .readFile(new File(gitManager.getRootDir(), binaryFileName))
                        .map(data -> new BinaryFile(
                            binaryFileName,
                            Base64.encodeToString(data, Base64.DEFAULT)
                        ));
                  })
                  .toList()
                  .flatMap(binaryFiles -> Observable.just(new PreviewRequestData(
                      getPreviewCloneUrl(gitManager),
                      nonBinaryDiff,
                      binaryFiles,
                      clientSecret)));
            })
        );
  }

  private String getPreviewCloneUrl(GitManager gitManager) {
    // TODO this is not that great security wise. In the long run use https://help.github.com/articles/git-automation-with-oauth-tokens/
    return "https://"
        + loginManager.getAccount().getAccessToken()
        + ":x-oauth-basic@"
        + gitManager.getRepositoro().getCloneUrl().replaceFirst("https://", "");
  }

  private Observable<Set<String>> getChangedBinaryFiles(GitManager gitManager) {
    return gitManager
        .status()
        .flatMap(status -> {
          File rootDir = gitManager.getRootDir();

          // all changed files
          Set<String> allFiles = status.getUncommittedChanges();
          allFiles.addAll(status.getUntracked());
          allFiles.addAll(status.getUntrackedFolders());
          allFiles.removeAll(status.getMissing());
          Iterator<String> filesIterator = allFiles.iterator();
          while (filesIterator.hasNext()) {
            if (new File(rootDir, filesIterator.next()).isDirectory()) {
              filesIterator.remove();
            }
          }

          // changed binary files
          Set<String> binaryFiles = new HashSet<>();
          for (String changedFile : allFiles) {
            try {
              if (fileUtils.isBinary(new File(rootDir, changedFile))) {
                binaryFiles.add(changedFile);
              }
            } catch (IOException ioe) {
              return Observable.error(ioe);
            }
          }

          return Observable.just(binaryFiles);
        });
  }

}
