package org.faudroids.mrhyde.git;

import com.google.common.base.Optional;

import org.eclipse.egit.github.core.Tree;

/**
 * Collection of various attributes which can be cached.
 */
class MetaDataCache {

	private Tree tree;
	private String baseCommitSha;

	public void cacheTree(Tree tree) {
		this.tree = tree;
	}

	public Optional<Tree> getTree() {
		return Optional.fromNullable(tree);
	}

	public void cacheBaseCommitSha(String baseCommitSha) {
		this.baseCommitSha = baseCommitSha;
	}

	public Optional<String> getBaseCommitSha() {
		return Optional.fromNullable(baseCommitSha);
	}

	public void reset() {
		tree = null;
		baseCommitSha = null;
	}

}
