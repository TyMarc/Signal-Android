package org.thoughtcrime.securesms.search.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents the information for a single item in a {@link SearchResult}.
 */
public class SearchResultInfo {

  public final Uri    thumbnailUri;
  public final String title;
  public final String subtitle;
  public final String sideDescriptor;
  public final Type   type;

  public SearchResultInfo(@Nullable Uri    thumbnailUri,
                          @NonNull  String title,
                          @Nullable String subtitle,
                          @Nullable String sideDescriptor,
                          @NonNull  Type   type)
  {
    this.thumbnailUri   = thumbnailUri;
    this.title          = title;
    this.subtitle       = subtitle;
    this.sideDescriptor = sideDescriptor;
    this.type           = type;
  }

  public enum Type {
    CONVERSATION, MESSAGE, CONTACT
  }
}
