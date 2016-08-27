package org.faudroids.mrhyde.jekyll;

import android.content.Context;

import org.eclipse.egit.github.core.Repository;
import org.faudroids.mrhyde.git.FileManagerFactory;
import org.faudroids.mrhyde.github.GitHubRepository;

import javax.inject.Inject;

public class JekyllManagerFactory {

	private final Context context;
	private final FileManagerFactory fileManagerFactory;

	@Inject
	JekyllManagerFactory(Context context, FileManagerFactory fileManagerFactory) {
		this.context = context;
		this.fileManagerFactory = fileManagerFactory;
	}


	public JekyllManager createJekyllManager(GitHubRepository repository) {
		return new JekyllManager(context, fileManagerFactory.createFileManager(repository));
	}

}
