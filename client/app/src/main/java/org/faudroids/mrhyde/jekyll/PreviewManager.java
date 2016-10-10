package org.faudroids.mrhyde.jekyll;


import android.content.Context;
import android.util.Base64;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.auth.Account;
import org.faudroids.mrhyde.auth.AccountVisitor;
import org.faudroids.mrhyde.auth.LoginManager;
import org.faudroids.mrhyde.auth.OAuthAccessTokenProvider;
import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.git.FileUtils;
import org.faudroids.mrhyde.git.GitManager;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.github.GitHubAccount;
import org.faudroids.mrhyde.gitlab.GitLabAccount;

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
  private final OAuthAccessTokenProvider accessTokenProvider;
  private final FileUtils fileUtils;
  private final PreviewCloneUrlCreator cloneUrlCreator = new PreviewCloneUrlCreator();

  @Inject
  PreviewManager(
      Context context,
      JekyllApi jekyllApi,
      LoginManager loginManager,
      OAuthAccessTokenProvider accessTokenProvider,
      FileUtils fileUtils) {

    this.jekyllApi = jekyllApi;
    this.clientSecret = context.getString(R.string.jekyllServerClientSecret);
    this.loginManager = loginManager;
    this.accessTokenProvider = accessTokenProvider;
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
    Repository repository = gitManager.getRepository();
    Account account = loginManager.getAccount(gitManager.getRepository());

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
                        .readFile(new File(repository.getRootDir(), binaryFileName))
                        .map(data -> new BinaryFile(
                            binaryFileName,
                            Base64.encodeToString(data, Base64.DEFAULT)
                        ));
                  })
                  .toList()
                  .map(binaryFiles -> new PreviewRequestData(
                      account.accept(cloneUrlCreator, repository).toBlocking().first(),
                      nonBinaryDiff,
                      binaryFiles,
                      clientSecret)
                  );
            })
        );
  }

  private Observable<Set<String>> getChangedBinaryFiles(GitManager gitManager) {
    return gitManager
        .status()
        .flatMap(status -> {
          File rootDir = gitManager.getRepository().getRootDir();

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

  private class PreviewCloneUrlCreator implements AccountVisitor<Repository, Observable<String>> {

    @Override
    public Observable<String> visit(GitHubAccount account, Repository repository) {
      // TODO this is not that great security wise. In the long run use https://help.github.com/articles/git-automation-with-oauth-tokens/
      return accessTokenProvider
          .visit(account, null)
          .map(accessToken -> String.format("https://%s:x-oauth-basic@%s",
              accessToken,
              repository.getCloneUrl().replaceFirst("https://", "")
          ));
    }

    @Override
    public Observable<String> visit(BitbucketAccount account, Repository repository) {
      // TODO same considerations as above
      return accessTokenProvider
          .visit(account, null)
          // bitbucket uses the format https://<username>@bitbucket.org/<repo>
          .map(accessToken -> String.format("https://x-token-auth:%s@%s",
              accessToken,
              repository.getCloneUrl().split("@")[1]
          ));
    }

    @Override
    public Observable<String> visit(GitLabAccount account, Repository param) {
      // TODO
      throw new UnsupportedOperationException();
    }

  }

}
