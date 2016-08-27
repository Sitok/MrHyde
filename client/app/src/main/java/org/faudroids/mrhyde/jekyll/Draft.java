package org.faudroids.mrhyde.jekyll;

import org.faudroids.mrhyde.git.FileNode;

import java.io.File;

/**
 * One Jekyll draft comparable with other drafts via its title.
 */
public class Draft extends AbstractJekyllContent implements Comparable<Draft> {

  @Deprecated
	public Draft(String title, FileNode fileNode) {
		super(title, fileNode);
	}

  public Draft(String title, File file) {
    super(title, file);
  }


	@Override
	public int compareTo(Draft another) {
		return title.compareTo(another.title);
	}
}
