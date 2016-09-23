package org.faudroids.mrhyde.git;

import com.google.common.base.Objects;

import org.eclipse.jgit.lib.Ref;

/**
 * Meta data about a single git branch.
 */
public class Branch {

  private final String fullBranchName;

  public Branch(Ref ref) {
    this.fullBranchName = ref.getName();
  }

  public String getName() {
    return fullBranchName;
  }

  public String getDisplayName() {
    String[] paths = fullBranchName.split("/");
    return paths[paths.length - 1];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Branch)) return false;
    Branch branch = (Branch) o;
    return Objects.equal(fullBranchName, branch.fullBranchName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(fullBranchName);
  }
}
