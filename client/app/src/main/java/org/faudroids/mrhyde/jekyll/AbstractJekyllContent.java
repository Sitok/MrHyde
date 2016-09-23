package org.faudroids.mrhyde.jekyll;

import com.google.common.base.Objects;

import java.io.File;

/**
 * General Jeklly content.
 */
public abstract class AbstractJekyllContent {

	protected final String title;
  protected final File file;

  public AbstractJekyllContent(String title, File file) {
    this.title = title;
    this.file = file;
  }

	public String getTitle() {
		return title;
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
        Objects.equal(file, that.file);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(title, file);
  }
}
