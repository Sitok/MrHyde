package org.faudroids.mrhyde.app;

import org.faudroids.mrhyde.bitbucket.BitbucketModule;
import org.faudroids.mrhyde.git.CloneRepositoryService;
import org.faudroids.mrhyde.git.GitModule;
import org.faudroids.mrhyde.github.GitHubModule;
import org.faudroids.mrhyde.gitlab.GitLabModule;
import org.faudroids.mrhyde.jekyll.JekyllModule;
import org.faudroids.mrhyde.ui.BitbucketLoginActivity;
import org.faudroids.mrhyde.ui.CloneBitbucketRepoActivity;
import org.faudroids.mrhyde.ui.CloneGitHubRepoActivity;
import org.faudroids.mrhyde.ui.CloneGitLabRepoActivity;
import org.faudroids.mrhyde.ui.ClonedReposActivity;
import org.faudroids.mrhyde.ui.CommitActivity;
import org.faudroids.mrhyde.ui.DirActivity;
import org.faudroids.mrhyde.ui.DraftsActivity;
import org.faudroids.mrhyde.ui.GitHubLoginActivity;
import org.faudroids.mrhyde.ui.GitLabLoginActivity;
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
@Component(modules = {AppModule.class, GitHubModule.class, JekyllModule.class, GitModule.class,
    BitbucketModule.class, GitLabModule.class})
public interface AppComponent {
  void inject(MrHydeApp app);
  void inject(WelcomeActivity activity);
  void inject(GitHubLoginActivity activity);
  void inject(BitbucketLoginActivity activity);
  void inject(GitLabLoginActivity activity);
  void inject(ClonedReposActivity activity);
  void inject(CloneGitHubRepoActivity activity);
  void inject(CloneBitbucketRepoActivity activity);
  void inject(CloneGitLabRepoActivity activity);
  void inject(SettingsActivity activity);
  void inject(PostsActivity activity);
  void inject(DraftsActivity activity);
  void inject(SelectDirActivity activity);
  void inject(DirActivity activity);
  void inject(TextEditorActivity activity);
  void inject(CommitActivity activity);
  void inject(RepoOverviewActivity activity);
  void inject(ImageViewerActivity activity);
  void inject(PreviewActivity activity);
  void inject(CloneRepositoryService service);
}
