package org.thoughtcrime.securesms.search.model;

import android.support.annotation.NonNull;

/**
 * Represents a search result for a group.
 */
public class ConversationResult {

  public final String title;
  public final String subtitle;

  public ConversationResult(@NonNull String title, @NonNull String subtitle) {
    this.title    = title;
    this.subtitle = subtitle;
  }
}
