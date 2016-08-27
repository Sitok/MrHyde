package org.faudroids.mrhyde.jekyll;

import com.google.common.base.Objects;

import org.faudroids.mrhyde.git.FileNode;

import java.io.File;

/**
 * General Jeklly content.
 */
public abstract class AbstractJekyllContent {

	protected final String title;
	protected final FileNode fileNode;
  protected final File file;

  @Deprecated
	public AbstractJekyllContent(String title, FileNode fileNode) {
		this.title = title;
		this.fileNode = fileNode;
    this.file = null;
	}

  public AbstractJekyllContent(String title, File file) {
    this.title = title;
    this.file = file;
    this.fileNode = null;
  }

	public String getTitle() {
		return title;
	}

	public FileNode getFileNode() {
		return fileNode;
	}

  public File getFile() {
    return file;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AbstractJekyllContent)) return false;
    AbstractJekyllContent that = (AbstractJekyllContent) o;
    return Objects.equal(title, that.title) &&
        Objects.equal(fileNode, that.fileNode) &&
        Objects.equal(file, that.file);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(title, fileNode, file);
  }
}
