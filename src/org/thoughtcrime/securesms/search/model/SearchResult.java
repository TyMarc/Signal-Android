package org.thoughtcrime.securesms.search.model;

import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.recipients.Recipient;

import java.util.Collections;
import java.util.List;

/**
 * Represents an all-encompassing search result that can contain various result for different
 * subcategories.
 */
public class SearchResult {

  public static final SearchResult EMPTY = new SearchResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

  public final List<Recipient>     contacts;
  public final List<ThreadRecord>  conversations;
  public final List<MessageResult> messages;

  public SearchResult(@NonNull List<Recipient>     contacts,
                      @NonNull List<ThreadRecord>  conversations,
                      @NonNull List<MessageResult> messages)
  {
    this.contacts      = Collections.unmodifiableList(contacts);
    this.conversations = Collections.unmodifiableList(conversations);
    this.messages      = Collections.unmodifiableList(messages);
  }

  public int size() {
    return contacts.size() + conversations.size() + messages.size();
  }
}
