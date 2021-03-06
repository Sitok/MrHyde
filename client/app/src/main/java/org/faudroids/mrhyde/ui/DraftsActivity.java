package org.faudroids.mrhyde.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Optional;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.jekyll.Draft;
import org.faudroids.mrhyde.jekyll.Post;
import org.faudroids.mrhyde.ui.utils.JekyllUiUtils;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import rx.Observable;

public class DraftsActivity extends AbstractJekyllActivity<Draft> {

	private static final String HELP_PUBLISH_DRAFTS = "HELP_PUBLISH_DRAFTS";

	@Inject HelpManager helpManager;
	@BindView(R.id.card_help) protected View helpView;

	public DraftsActivity() {
		super(
				R.string.drafts,
				R.string.no_drafts,
				R.string.action_publish_draft,
				R.string.draft_published,
				R.string.publish_draft_title,
				R.string.publish_draft_message);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
		super.onCreate(savedInstanceState);

		// setup help for publishing drafts
		if (helpManager.shouldDisplayHelp(HELP_PUBLISH_DRAFTS)) {
			helpManager.setupHelpView(HELP_PUBLISH_DRAFTS, helpView, R.string.help_publish_draft_title, R.string.help_publish_draft_message);
		}
	}

	@Override
	protected void onAddClicked(JekyllUiUtils.OnContentCreatedListener<Draft> contentListener) {
		jekyllUiUtils.showNewDraftDialog(this, jekyllManager, repository, Optional.<File>absent(), contentListener);
	}

	@Override
	protected Observable<List<Draft>> doLoadItems() {
		return jekyllManager.getAllDrafts();
	}

	@Override
	protected AbstractAdapter createAdapter() {
		return new DraftsAdapter();
	}

	@Override
	protected Observable<Post> createMoveObservable(Draft draft) {
		return jekyllManager.publishDraft(draft);
	}

	@Override
	protected String getMovedFilenameForItem(Draft draft) {
		return "_posts/" + jekyllManager.postTitleToFilename(draft.getTitle());
	}

	public class DraftsAdapter extends AbstractAdapter {

		@Override
		public AbstractViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_overview_draft, parent, false);
			return new DraftViewHolder(view);
		}

		public class DraftViewHolder extends AbstractViewHolder {

			public DraftViewHolder(View view) {
				super(view);
			}

			@Override
			protected void doSetItem(Draft item) {
				jekyllUiUtils.setDraftOverview(view, item);
			}

		}
	}

}
