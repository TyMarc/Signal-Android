package org.thoughtcrime.securesms.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.search.model.SearchResult;
import org.thoughtcrime.securesms.search.model.SearchResultInfo;
import org.thoughtcrime.securesms.util.StickyHeaderDecoration;

public class SearchListAdapter extends    RecyclerView.Adapter<SearchListAdapter.SearchResultViewHolder>
                               implements StickyHeaderDecoration.StickyHeaderAdapter<SearchListAdapter.HeaderViewHolder> {

  @NonNull
  private SearchResult searchResult = SearchResult.EMPTY;

  @NonNull
  @Override
  public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new SearchResultViewHolder(LayoutInflater.from(parent.getContext())
                                                    .inflate(R.layout.item_search_result, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
    holder.bind(getResultInfo(position));
  }

  @Override
  public int getItemCount() {
    return searchResult.size();
  }

  @Override
  public long getHeaderId(int position) {
    return getResultInfo(position).type.ordinal();
  }

  @Override
  public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
    return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.header_search_result, parent, false));
  }

  @Override
  public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
    viewHolder.bind(getResultInfo(position).type);
  }

  public void updateResults(@NonNull SearchResult result) {
    this.searchResult = result;
    notifyDataSetChanged();
  }

  private SearchResultInfo getResultInfo(int position) {
    int firstContactIndex = searchResult.conversations.size();
    int firstMessageIndex = firstContactIndex + searchResult.contacts.size();

    if (position < firstContactIndex) {
      return searchResult.conversations.get(position);
    } else if (position < firstMessageIndex) {
      return searchResult.contacts.get(position - firstContactIndex);
    } else if (position < searchResult.size()) {
      return searchResult.messages.get(position - firstMessageIndex);
    } else {
      throw new IndexOutOfBoundsException("Requested search result out of bounds. Requested: " + position + ", length: " + searchResult.size());
    }
  }

  public static class SearchResultViewHolder extends RecyclerView.ViewHolder {

    private final ImageView avatarView;
    private final TextView  titleView;
    private final TextView  subtitleView;
    private final TextView  sideDescriptorView;

    public SearchResultViewHolder(View itemView) {
      super(itemView);
      avatarView         = itemView.findViewById(R.id.search_item_avatar);
      titleView          = itemView.findViewById(R.id.search_item_title);
      subtitleView       = itemView.findViewById(R.id.search_item_subtitle);
      sideDescriptorView = itemView.findViewById(R.id.search_item_side_descriptor);
    }

    void bind(@NonNull SearchResultInfo resultInfo) {
      avatarView.setImageResource(R.drawable.ic_contact_picture);
      titleView.setText(resultInfo.title);
      subtitleView.setText(resultInfo.subtitle);
      sideDescriptorView.setText(resultInfo.sideDescriptor);
    }
  }

  public static class HeaderViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;

    public HeaderViewHolder(View itemView) {
      super(itemView);
      titleView = (TextView) itemView;
    }

    public void bind(@NonNull SearchResultInfo.Type resultType) {
      switch (resultType) {
        case CONVERSATION:
          titleView.setText("Conversations");
          break;
        case CONTACT:
          titleView.setText("Contacts");
          break;
        case MESSAGE:
          titleView.setText("Messages");
          break;
      }
    }
  }
}
