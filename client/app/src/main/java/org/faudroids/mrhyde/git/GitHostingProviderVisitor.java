package org.faudroids.mrhyde.git;

/**
 * Modified visitor pattern for {@link GitHostingProvider}.
 */
public interface GitHostingProviderVisitor<P,R> {

  R visitGitHub(P param);
  R visitBitbucket(P param);
  R visitGitLab(P param);

}
