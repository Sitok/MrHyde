package org.faudroids.mrhyde.app;

import org.faudroids.mrhyde.git.GitModule;
import org.faudroids.mrhyde.github.GitHubModule;
import org.faudroids.mrhyde.jekyll.JekyllModule;
import org.faudroids.mrhyde.ui.AllReposFragment;
import org.faudroids.mrhyde.ui.CommitActivity;
import org.faudroids.mrhyde.ui.DirActivity;
import org.faudroids.mrhyde.ui.DraftsActivity;
import org.faudroids.mrhyde.ui.FavouriteReposFragment;
import org.faudroids.mrhyde.ui.ImageViewerActivity;
import org.faudroids.mrhyde.ui.LoginActivity;
import org.faudroids.mrhyde.ui.MainDrawerActivity;
import org.faudroids.mrhyde.ui.PostsActivity;
import org.faudroids.mrhyde.ui.PreviewActivity;
import org.faudroids.mrhyde.ui.RepoOverviewActivity;
import org.faudroids.mrhyde.ui.SelectDirActivity;
import org.faudroids.mrhyde.ui.SelectRepoActivity;
import org.faudroids.mrhyde.ui.SettingsFragment;
import org.faudroids.mrhyde.ui.TextEditorActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, GitHubModule.class, JekyllModule.class, GitModule.class})
public interface AppComponent {
  void inject(MrHydeApp app);
  void inject(LoginActivity activity);
  void inject(MainDrawerActivity activity);
  void inject(SelectRepoActivity activity);
  void inject(PostsActivity activity);
  void inject(DraftsActivity activity);
  void inject(SelectDirActivity activity);
  void inject(DirActivity activity);
  void inject(TextEditorActivity activit);
  void inject(CommitActivity activit);
  void inject(RepoOverviewActivity activit);
  void inject(ImageViewerActivity activit);
  void inject(PreviewActivity activit);
  void inject(AllReposFragment fragment);
  void inject(FavouriteReposFragment fragment);
  void inject(SettingsFragment fragment);
}
