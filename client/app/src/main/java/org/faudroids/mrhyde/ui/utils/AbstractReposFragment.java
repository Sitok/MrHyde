package org.faudroids.mrhyde.ui.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.github.GitHubManager;
import org.faudroids.mrhyde.github.GitHubRepository;
import org.faudroids.mrhyde.ui.RepoOverviewActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public abstract class AbstractReposFragment extends AbstractFragment {

  private static final int REQUEST_OVERVIEW = 41; // used to mark the end of an overview activity
  private static final DateFormat dateFormat = DateFormat.getDateInstance();

  @Inject protected GitHubManager gitHubManager;

  @BindView(R.id.list) protected RecyclerView recyclerView;
  protected RepositoryAdapter repoAdapter;
  private RecyclerView.LayoutManager layoutManager;


  public AbstractReposFragment() {
    this(R.layout.fragment_repos);
  }


  public AbstractReposFragment(int layoutResource) {
    super(layoutResource);
  }


  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // setup list
    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    repoAdapter = new RepositoryAdapter();
    recyclerView.setAdapter(repoAdapter);
    loadRepositories();
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_OVERVIEW:
        loadRepositories();
        return;
    }
  }


  protected abstract void loadRepositories();


  protected void onRepositorySelected(GitHubRepository repository) {
    Intent intent = new Intent(AbstractReposFragment.this.getActivity(), RepoOverviewActivity.class);
    intent.putExtra(RepoOverviewActivity.EXTRA_REPOSITORY, repository);
    startActivityForResult(intent, REQUEST_OVERVIEW);
  }


  protected final class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.RepoViewHolder> {

    private final List<GitHubRepository> repositoryList = new ArrayList<>();


    @Override
    public RepoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repo, parent, false);
      return new RepoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RepoViewHolder holder, int position) {
      holder.setRepo(repositoryList.get(position));
    }


    @Override
    public int getItemCount() {
      return repositoryList.size();
    }


    public void setItems(Collection<GitHubRepository> repositoryList) {
      this.repositoryList.clear();
      this.repositoryList.addAll(repositoryList);
      Collections.sort(
          this.repositoryList,
          (lhs, rhs) -> lhs.getFullName().compareTo(rhs.getFullName())
      );
      notifyDataSetChanged();
    }


    public class RepoViewHolder extends RecyclerView.ViewHolder {

      private final View containerView;
      private final ImageView iconView;
      private final TextView titleView, subTitleView;
      private final ImageView heartView;

      public RepoViewHolder(View view) {
        super(view);
        this.containerView = view.findViewById(R.id.container);
        this.iconView = (ImageView) view.findViewById(R.id.icon);
        this.titleView = (TextView) view.findViewById(R.id.title);
        this.subTitleView = (TextView) view.findViewById(R.id.subtitle);
        this.heartView = (ImageView) view.findViewById(R.id.heart);
      }

      public void setRepo(final GitHubRepository repo) {
        Picasso.with(getActivity())
            .load(repo.getOwner().getAvatarUrl())
            .resizeDimen(R.dimen.card_icon_size, R.dimen.card_icon_size)
            .placeholder(R.drawable.octocat_black)
            .transform(new CircleTransformation())
            .into(iconView);
        titleView.setText(repo.getFullName());
        if (repo.getPushedAt() != null)
          subTitleView.setText(getString(R.string.repos_last_update, dateFormat.format(repo.getPushedAt())));
        containerView.setOnClickListener(v -> onRepositorySelected(repo));
        if (gitHubManager.isRepositoryFavourite(repo)) {
          heartView.setVisibility(View.VISIBLE);
          heartView.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
              .setTitle(AbstractReposFragment.this.getActivity().getString(R.string.unmark_title))
              .setMessage(AbstractReposFragment.this.getActivity().getString(R.string.unmark_message))
              .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                gitHubManager.unmarkRepositoryAsFavourite(repo);
                loadRepositories();
              })
              .setNegativeButton(android.R.string.cancel, null)
              .show());
        } else {
          heartView.setVisibility(View.GONE);
        }
      }
    }

  }

}
