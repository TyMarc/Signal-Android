package org.thoughtcrime.securesms.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thoughtcrime.securesms.ConversationListItem;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.search.model.MessageResult;
import org.thoughtcrime.securesms.search.model.SearchResult;
import org.thoughtcrime.securesms.util.StickyHeaderDecoration;

import java.util.Collections;
import java.util.Locale;

public class SearchListAdapter extends    RecyclerView.Adapter<SearchListAdapter.SearchResultViewHolder>
                               implements StickyHeaderDecoration.StickyHeaderAdapter<SearchListAdapter.HeaderViewHolder> {

  private static final int TYPE_CONVERSATIONS = 1;
  private static final int TYPE_CONTACTS      = 2;
  private static final int TYPE_MESSAGES      = 3;

  private final GlideRequests glideRequests;

  @NonNull
  private SearchResult searchResult = SearchResult.EMPTY;

  public SearchListAdapter(@NonNull GlideRequests glideRequests) {
    this.glideRequests = glideRequests;
  }

  @NonNull
  @Override
  public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new SearchResultViewHolder(LayoutInflater.from(parent.getContext())
                                                    .inflate(R.layout.conversation_list_item_view, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
    // TODO: Make nicer -- maybe move all position stuff in here?

    ThreadRecord conversationResult = getConversationResult(position);
    if (conversationResult != null) {
      holder.bind(conversationResult, glideRequests);
    }

    Recipient contactResult = getContactResult(position);
    if (contactResult != null) {
      holder.bind(contactResult, glideRequests);
    }

    MessageResult messageResult = getMessageResult(position);
    if (messageResult != null) {
      holder.bind(messageResult, glideRequests);
    }
  }

  @Override
  public void onViewRecycled(SearchResultViewHolder holder) {
    holder.recycle();
  }

  @Override
  public int getItemCount() {
    return searchResult.size();
  }

  @Override
  public long getHeaderId(int position) {
    if (getConversationResult(position) != null) {
      return TYPE_CONVERSATIONS;
    } else if (getContactResult(position) != null) {
      return TYPE_CONTACTS;
    } else {
      return TYPE_MESSAGES;
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
  private ThreadRecord getConversationResult(int position) {
    if (position < searchResult.conversations.size()) {
      return searchResult.conversations.get(position);
    }
    return null;
  }

  @Nullable
  private Recipient getContactResult(int position) {
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

    private final ConversationListItem root;

    public SearchResultViewHolder(View itemView) {
      super(itemView);
      root = (ConversationListItem) itemView;
    }

    void bind(@NonNull ThreadRecord conversationResult, @NonNull GlideRequests glideRequests) {
      // TODO: Locale
      root.bind(conversationResult, glideRequests, Locale.getDefault(), Collections.emptySet(), false);
    }

    void bind(@NonNull Recipient contactResult, @NonNull GlideRequests glideRequests) {
      root.bind(contactResult, glideRequests);
    }

    void bind(@NonNull MessageResult messageResult, @NonNull GlideRequests glideRequests) {
      // TODO: Locale
      root.bind(messageResult, glideRequests, Locale.getDefault());
    }

    void recycle() {
      root.unbind();
    }
  }

  public static class HeaderViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;

    public HeaderViewHolder(View itemView) {
      super(itemView);
      titleView = (TextView) itemView;
    }

    public void bind(int headerType) {
      // TODO: Strings
      switch (headerType) {
        case TYPE_CONVERSATIONS:
          titleView.setText("Conversations");
          break;
        case TYPE_CONTACTS:
          titleView.setText("Contacts");
          break;
        case TYPE_MESSAGES:
          titleView.setText("Messages");
          break;
      }
    }
  }
}
