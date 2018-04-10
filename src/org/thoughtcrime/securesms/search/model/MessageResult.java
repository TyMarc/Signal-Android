package org.thoughtcrime.securesms.search.model;

import android.net.Uri;

/**
 * Represents a search result for a message
 */
public class MessageResult {

  public final Uri    avatarUri;
  public final String body;
  public final long   receivedTimestampMs;

  public MessageResult(Uri avatarUri, String body, long receivedTimestampMs) {
    this.avatarUri           = avatarUri;
    this.body                = body;
    this.receivedTimestampMs = receivedTimestampMs;
  }
}
