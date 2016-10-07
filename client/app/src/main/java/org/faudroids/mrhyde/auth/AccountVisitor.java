package org.faudroids.mrhyde.auth;

import org.faudroids.mrhyde.bitbucket.BitbucketAccount;
import org.faudroids.mrhyde.github.GitHubAccount;

/**
 * Visitor pattern for {@link Account}.
 */
public interface AccountVisitor<P, R> {
  R visit(GitHubAccount account, P param);
  R visit(BitbucketAccount account, P param);
}
