package org.faudroids.mrhyde.jekyll;

import android.content.Context;

import org.faudroids.mrhyde.git.FileUtils;
import org.faudroids.mrhyde.git.GitManager;

import javax.inject.Inject;

public class JekyllManagerFactory {

	private final Context context;
  private final FileUtils fileUtils;

	@Inject
	JekyllManagerFactory(Context context, FileUtils fileUtils) {
		this.context = context;
    this.fileUtils = fileUtils;
	}


	public JekyllManager createJekyllManager(GitManager gitManager) {
		return new JekyllManager(context, fileUtils, gitManager);
	}

}
