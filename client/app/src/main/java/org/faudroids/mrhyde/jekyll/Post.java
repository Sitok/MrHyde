package org.faudroids.mrhyde.jekyll;

import com.google.common.base.Objects;

import org.faudroids.mrhyde.git.FileNode;

import java.io.File;
import java.util.Date;

/**
 * One Jekyll post. Can be compared to other posts via the creation date.
 */
public class Post extends AbstractJekyllContent implements Comparable<Post> {

	private final Date date;

  @Deprecated
	public Post(String title, Date date, FileNode fileNode) {
		super(title, fileNode);
		this.date = date;
	}

  public Post(String title, Date date, File file) {
    super(title, file);
    this.date = date;
  }


	public Date getDate() {
		return date;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Post post = (Post) o;
		return Objects.equal(date, post.date);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), date);
	}


	@Override
	public int compareTo(Post another) {
		return date.compareTo(another.date);
	}

}
