package org.faudroids.mrhyde.app;

import org.faudroids.mrhyde.bitbucket.BitbucketModule;
import org.faudroids.mrhyde.git.CloneRepositoryService;
import org.faudroids.mrhyde.git.GitModule;
import org.faudroids.mrhyde.github.GitHubModule;
import org.faudroids.mrhyde.jekyll.JekyllModule;
import org.faudroids.mrhyde.ui.BitbucketLoginActivity;
import org.faudroids.mrhyde.ui.CloneBitbucketRepoActivity;
import org.faudroids.mrhyde.ui.CloneGitHubRepoActivity;
import org.faudroids.mrhyde.ui.ClonedReposActivity;
import org.faudroids.mrhyde.ui.CommitActivity;
import org.faudroids.mrhyde.ui.DirActivity;
import org.faudroids.mrhyde.ui.DraftsActivity;
import org.faudroids.mrhyde.ui.GitHubLoginActivity;
import org.faudroids.mrhyde.ui.ImageViewerActivity;
import org.faudroids.mrhyde.ui.PostsActivity;
import org.faudroids.mrhyde.ui.PreviewActivity;
import org.faudroids.mrhyde.ui.RepoOverviewActivity;
import org.faudroids.mrhyde.ui.SelectDirActivity;
import org.faudroids.mrhyde.ui.SettingsActivity;
import org.faudroids.mrhyde.ui.TextEditorActivity;
import org.faudroids.mrhyde.ui.WelcomeActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, GitHubModule.class, JekyllModule.class, GitModule.class, BitbucketModule.class})
public interface AppComponent {
  void inject(MrHydeApp app);
  void inject(WelcomeActivity activity);
  void inject(GitHubLoginActivity activity);
  void inject(BitbucketLoginActivity activity);
  void inject(ClonedReposActivity activity);
  void inject(CloneGitHubRepoActivity activity);
  void inject(CloneBitbucketRepoActivity activity);
  void inject(SettingsActivity activity);
  void inject(PostsActivity activity);
  void inject(DraftsActivity activity);
  void inject(SelectDirActivity activity);
  void inject(DirActivity activity);
  void inject(TextEditorActivity activit);
  void inject(CommitActivity activit);
  void inject(RepoOverviewActivity activit);
  void inject(ImageViewerActivity activit);
  void inject(PreviewActivity activit);
  void inject(CloneRepositoryService service);
}
