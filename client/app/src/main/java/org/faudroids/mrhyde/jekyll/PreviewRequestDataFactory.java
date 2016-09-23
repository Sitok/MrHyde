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


/**
 * Unfortunately JGit does not seem to support binary diffs. As a result binary feels need to be
 * sent 'separately' to the preview server (base 64 encoded). This class is responsible for
 * getting the non binary Git diff, loading the binary files and packing everything into
 * a {@link PreviewRequestData}.
 */
public class PreviewRequestDataFactory {

	private final String clientSecret;
	private final LoginManager loginManager;
  private final FileUtils fileUtils;

	@Inject
  PreviewRequestDataFactory(
      Context context,
      LoginManager loginManager,
      FileUtils fileUtils) {
		this.clientSecret = context.getString(R.string.jekyllServerClientSecret);
		this.loginManager = loginManager;
    this.fileUtils = fileUtils;
	}


	public Observable<PreviewRequestData> createRepoDetails(GitManager gitManager) {
    return gitManager
        .diff()
        .flatMap(nonBinaryDiff -> {
          Timber.d("non binary diff is " + nonBinaryDiff);
          return getChangedBinaryFiles(gitManager)
              .flatMap(binaryFiles -> Observable
                  .from(binaryFiles)
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
                  .flatMap(binaryFiles1 -> {
                    // TODO this is not that great security wise. In the long run use https://help.github.com/articles/git-automation-with-oauth-tokens/
                    String cloneUrl = "https://"
                        + loginManager.getAccount().getAccessToken()
                        + ":x-oauth-basic@"
                        + gitManager.getRepositoro().getCloneUrl().replaceFirst("https://", "");
                    return Observable.just(new PreviewRequestData(
                        cloneUrl,
                        nonBinaryDiff,
                        binaryFiles1,
                        clientSecret));
                  }));
        });
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
