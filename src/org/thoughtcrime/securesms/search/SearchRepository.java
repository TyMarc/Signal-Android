package org.thoughtcrime.securesms.search;

import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.search.model.SearchResult;
import org.thoughtcrime.securesms.search.model.SearchResultInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Manages data retrieval for search.
 */
public class SearchRepository {

  private final Executor executor;

  public SearchRepository(@NonNull Executor executor) {
    this.executor = executor;
  }

  @NonNull
  public void query(@NonNull String query, @NonNull Callback callback) {
    executor.execute(() -> {
      List<SearchResultInfo> conversations = new ArrayList<>();
      conversations.add(new SearchResultInfo(null, query + ": Conversation 1", "Message text 1", "1:00pm", SearchResultInfo.Type.CONVERSATION));
      conversations.add(new SearchResultInfo(null, query + ": Conversation 2", "Message text 2", "2:00pm", SearchResultInfo.Type.CONVERSATION));

      List<SearchResultInfo> contacts = new ArrayList<>();
      conversations.add(new SearchResultInfo(null, query + ": Contact 1", null, "mobile", SearchResultInfo.Type.CONTACT));
      conversations.add(new SearchResultInfo(null, query + ": Contact 2", null, "home", SearchResultInfo.Type.CONTACT));

      List<SearchResultInfo> messages = new ArrayList<>();
      conversations.add(new SearchResultInfo(null, query + ": Message 1", null, "1:01pm", SearchResultInfo.Type.MESSAGE));
      conversations.add(new SearchResultInfo(null, query + ": Message 2", null, "2:01pm", SearchResultInfo.Type.MESSAGE));

      callback.onResult(new SearchResult(conversations, contacts, messages));
    });
  }

  public interface Callback {
    void onResult(@NonNull SearchResult result);
  }
}
