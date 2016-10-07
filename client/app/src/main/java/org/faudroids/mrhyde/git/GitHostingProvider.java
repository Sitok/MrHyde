package org.faudroids.mrhyde.git;

/**
 * Supported hosting providers.
 */
public enum GitHostingProvider {

  GITHUB {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitBitbucket(param);
    }
  },
  BITBUCKET {
    @Override
    public <P, R> R accept(GitHostingProviderVisitor<P, R> visitor, P param) {
      return visitor.visitBitbucket(param);
    }
  };

  public abstract <P,R> R accept(GitHostingProviderVisitor<P,R> visitor, P param);

}
