package org.faudroids.mrhyde.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.AvatarPlaceholder;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.ui.utils.CircleTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * {@link android.support.v7.widget.RecyclerView.Adapter} for {@link Repository}.
 */
final class RepositoryRecyclerViewAdapter extends RecyclerView.Adapter<RepositoryRecyclerViewAdapter.RepoViewHolder> {

  private final Context context;
  private final RepositorySelectionListener selectionListener;
  private final List<Repository> repositoryList = new ArrayList<>();
  private final AvatarPlaceholder avatarPlaceholder = new AvatarPlaceholder();


  public RepositoryRecyclerViewAdapter(Context context, RepositorySelectionListener selectionListener) {
    this.context = context;
    this.selectionListener = selectionListener;
  }

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

  public void setItems(Collection<Repository> repositoryList) {
    this.repositoryList.clear();
    List<Repository> nonFavoriteRepositories = new ArrayList<>(repositoryList);

    // sort alphabetically and by favorite status
    Collections.sort(
        nonFavoriteRepositories,
        (lhs, rhs) -> lhs.getFullName().toLowerCase().compareTo(rhs.getFullName().toLowerCase())
    );

    Iterator<Repository> iter = nonFavoriteRepositories.iterator();
    while (iter.hasNext()) {
      Repository repo = iter.next();
      if (!repo.isFavorite()) continue;
      this.repositoryList.add(repo);
      iter.remove();
    }
    this.repositoryList.addAll(nonFavoriteRepositories);
    notifyDataSetChanged();
  }


  public class RepoViewHolder extends RecyclerView.ViewHolder {

    private final View containerView;
    private final ImageView iconView;
    private final TextView titleView;
    private final TextView hostingProviderView;
    private final View heartView;

    public RepoViewHolder(View view) {
      super(view);
      this.containerView = view.findViewById(R.id.container);
      this.iconView = (ImageView) view.findViewById(R.id.icon);
      this.titleView = (TextView) view.findViewById(R.id.title);
      this.hostingProviderView = (TextView) view.findViewById(R.id.host_provider);
      this.heartView = view.findViewById(R.id.heart);
    }

    public void setRepo(final Repository repo) {
      Picasso.with(context)
          .load(repo.getOwner().get().getAvatarUrl().orNull())
          .resizeDimen(R.dimen.card_icon_size, R.dimen.card_icon_size)
          .placeholder(repo.accept(avatarPlaceholder, null))
          .transform(new CircleTransformation())
          .into(iconView);
      titleView.setText(repo.getFullName());
      hostingProviderView.setText(repo.getHostingProvider().getNameRes());
      containerView.setOnClickListener(v -> selectionListener.onRepositorySelected(repo));
      heartView.setVisibility(repo.isFavorite() ? View.VISIBLE : View.GONE);
    }
  }


  public interface RepositorySelectionListener {
    void onRepositorySelected(Repository repository);
  }

}
