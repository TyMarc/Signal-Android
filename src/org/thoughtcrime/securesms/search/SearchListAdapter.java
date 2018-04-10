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
import org.thoughtcrime.securesms.search.model.ContactResult;
import org.thoughtcrime.securesms.search.model.ConversationResult;
import org.thoughtcrime.securesms.search.model.MessageResult;
import org.thoughtcrime.securesms.search.model.SearchResult;
import org.thoughtcrime.securesms.search.model.SearchResultInfo;
import org.thoughtcrime.securesms.util.StickyHeaderDecoration;

public class SearchListAdapter extends    RecyclerView.Adapter<SearchListAdapter.SearchResultViewHolder>
                               implements StickyHeaderDecoration.StickyHeaderAdapter<SearchListAdapter.HeaderViewHolder> {

  private static final int HEADER_CONVERSATIONS = 1;
  private static final int HEADER_CONTACTS      = 2;
  private static final int HEADER_MESSAGES      = 3;

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
    // TODO: Make nicer -- maybe move all position stuff in here?

    ConversationResult conversationResult = getConversationResult(position);
    if (conversationResult != null) {
      holder.bind(conversationResult);
    }

    ContactResult contactResult = getContactResult(position);
    if (contactResult != null) {
      holder.bind(contactResult);
    }

    MessageResult messageResult = getMessageResult(position);
    if (messageResult != null) {
      holder.bind(messageResult);
    }
  }

  @Override
  public int getItemCount() {
    return searchResult.size();
  }

  @Override
  public long getHeaderId(int position) {
    if (getConversationResult(position) != null) {
      return HEADER_CONVERSATIONS;
    } else if (getContactResult(position) != null) {
      return HEADER_CONTACTS;
    } else {
      return HEADER_MESSAGES;
    }
  }

  @Override
  public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
    return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.header_search_result, parent, false));
  }

  @Override
  public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
    viewHolder.bind((int) getHeaderId(position));
  }

  public void updateResults(@NonNull SearchResult result) {
    this.searchResult = result;
    notifyDataSetChanged();
  }

  @Nullable
  private ConversationResult getConversationResult(int position) {
    if (position < searchResult.conversations.size()) {
      return searchResult.conversations.get(position);
    }
    return null;
  }

  @Nullable
  private ContactResult getContactResult(int position) {
    if (position >= getFirstContactIndex() && position < getFirstMessageIndex()) {
      return searchResult.contacts.get(position - getFirstContactIndex());
    }
    return null;
  }

  @Nullable
  private MessageResult getMessageResult(int position) {
    if (position >= getFirstMessageIndex() && position < searchResult.size()) {
      return searchResult.messages.get(position - getFirstMessageIndex());
    }
    return null;
  }

  private int getFirstContactIndex() {
    return searchResult.conversations.size();
  }

  private int getFirstMessageIndex() {
    return getFirstContactIndex() + searchResult.contacts.size();
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

    void bind(@NonNull ConversationResult conversationResult) {
      avatarView.setImageResource(R.drawable.ic_contact_picture);
      titleView.setText(conversationResult.title);
      subtitleView.setText(conversationResult.subtitle);
      sideDescriptorView.setText("1:00pm");
    }

    void bind(@NonNull ContactResult contactResult) {
      avatarView.setImageResource(R.drawable.ic_contact_picture);
      titleView.setText(contactResult.title);
      subtitleView.setText(contactResult.subtitle);
      sideDescriptorView.setText("1:00pm");
    }

    void bind(@NonNull MessageResult messageResult) {
      avatarView.setImageResource(R.drawable.ic_contact_picture);
      titleView.setText(String.valueOf(messageResult.receivedTimestampMs));
      subtitleView.setText(messageResult.body);
      sideDescriptorView.setText("1:00pm");
    }
  }

  public static class HeaderViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;

    public HeaderViewHolder(View itemView) {
      super(itemView);
      titleView = (TextView) itemView;
    }

    public void bind(int headerType) {
      switch (headerType) {
        case HEADER_CONVERSATIONS:
          titleView.setText("Conversations");
          break;
        case HEADER_CONTACTS:
          titleView.setText("Contacts");
          break;
        case HEADER_MESSAGES:
          titleView.setText("Messages");
          break;
      }
    }
  }
}
