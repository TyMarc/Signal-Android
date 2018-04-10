package org.thoughtcrime.securesms.search.model;

import android.support.annotation.NonNull;

/**
 * Represents a search result for a contact.
 */
public class ContactResult {

  public final String title;
  public final String subtitle;

  public ContactResult(@NonNull String title, @NonNull String subtitle) {
    this.title    = title;
    this.subtitle = subtitle;
  }
}
