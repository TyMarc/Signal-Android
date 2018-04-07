package org.thoughtcrime.securesms.search.model;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Represents an all-encompassing search result that can contain various result for different
 * subcategories.
 */
public class SearchResult {

  public static final SearchResult EMPTY = new SearchResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

  public final List<SearchResultInfo> contacts;
  public final List<SearchResultInfo> conversations;
  public final List<SearchResultInfo> messages;

  public SearchResult(@NonNull List<SearchResultInfo> contacts,
                      @NonNull List<SearchResultInfo> conversations,
                      @NonNull List<SearchResultInfo> messages)
  {
    this.contacts      = Collections.unmodifiableList(contacts);
    this.conversations = Collections.unmodifiableList(conversations);
    this.messages      = Collections.unmodifiableList(messages);
  }

  public int size() {
    return contacts.size() + conversations.size() + messages.size();
  }
}
