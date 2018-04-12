package org.thoughtcrime.securesms.search;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thoughtcrime.securesms.ConversationActivity;
import org.thoughtcrime.securesms.ConversationListActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.search.model.MessageResult;
import org.thoughtcrime.securesms.search.model.SearchResult;
import org.thoughtcrime.securesms.util.StickyHeaderDecoration;

import java.util.concurrent.Executors;

/**
 * A fragment that is displayed to do full-text search of messages, groups, and contacts.
 */
public class SearchFragment extends Fragment implements SearchListAdapter.EventListener {

  public static final String TAG = "SearchFragment";

  private TextView     noResultsView;
  private RecyclerView listView;

  private SearchViewModel   viewModel;
  private SearchListAdapter listAdapter;

  public static SearchFragment newInstance() {
    return new SearchFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Note: We essentially construct the dependency graph here. We can move this out in the future.
    SearchRepository searchRepository = new SearchRepository(getContext(),
                                                             DatabaseFactory.getSearchDatabase(getContext()),
                                                             DatabaseFactory.getContactsDatabase(getContext()),
                                                             DatabaseFactory.getThreadDatabase(getContext()),
                                                             ContactAccessor.getInstance(),
                                                             Executors.newSingleThreadExecutor());
    viewModel = ViewModelProviders.of(this, new SearchViewModel.Factory(searchRepository)).get(SearchViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_search, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    noResultsView = view.findViewById(R.id.search_no_results);
    listView      = view.findViewById(R.id.search_list);

    listAdapter = new SearchListAdapter(GlideApp.with(this), this);
    listView.setAdapter(listAdapter);
    listView.setLayoutManager(new LinearLayoutManager(getContext()));
    listView.addItemDecoration(new StickyHeaderDecoration(listAdapter, false, false));
  }

  @Override
  public void onStart() {
    super.onStart();
    viewModel.getSearchResult().observe(this, result -> {
      result = result != null ? result : SearchResult.EMPTY;

      listAdapter.updateResults(result);

      if (result.size() == 0) {
        noResultsView.setVisibility(View.VISIBLE);
        if (viewModel.getLastQuery().length() == 0) {
          noResultsView.setText(R.string.SearchFragment_begin_searching);
        } else {
          noResultsView.setText(getString(R.string.SearchFragment_no_results, viewModel.getLastQuery()));
        }
      } else {
        noResultsView.setVisibility(View.GONE);
      }
    });
  }

  @Override
  public void onConversationClicked(@NonNull ThreadRecord threadRecord) {
    ConversationListActivity conversationList = (ConversationListActivity) getActivity();
    if (conversationList != null) {
      conversationList.onCreateConversation(threadRecord.getThreadId(),
                                            threadRecord.getRecipient(),
                                            threadRecord.getDistributionType(),
                                            threadRecord.getLastSeen());
    }
  }

  @Override
  public void onContactClicked(@NonNull Recipient contact) {
    Intent intent = new Intent(getContext(), ConversationActivity.class);
    intent.putExtra(ConversationActivity.ADDRESS_EXTRA, contact.getAddress());

    long existingThread = DatabaseFactory.getThreadDatabase(getContext()).getThreadIdIfExistsFor(contact);

    intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, existingThread);
    intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
    startActivity(intent);
  }

  @SuppressLint("StaticFieldLeak")
  @Override
  public void onMessageClicked(@NonNull MessageResult message) {
    new AsyncTask<Void, Void, Pair<Long, Integer>>() {
      @Override
      protected Pair<Long, Integer> doInBackground(Void... voids) {
        long threadId         = DatabaseFactory.getThreadDatabase(getContext()).getThreadIdFor(message.recipient);
        int  startingPosition = DatabaseFactory.getMmsSmsDatabase(getContext()).getMessagePositionInConversation(threadId, message.receivedTimestampMs);
        startingPosition = Math.max(0, startingPosition);

        return new Pair<>(threadId, startingPosition);
      }

      @Override
      protected void onPostExecute(Pair<Long, Integer> data) {
        ConversationListActivity conversationList = (ConversationListActivity) getActivity();
        if (conversationList != null) {
          conversationList.openConversation(data.first,
                                            message.recipient,
                                            ThreadDatabase.DistributionTypes.DEFAULT,
                                            -1,
                                            data.second);
        }
      }
    }.execute();
  }

  public void updateSearchQuery(@NonNull String query) {
    Log.e(TAG, "query: " + query);
    if (viewModel != null) {
      viewModel.updateQuery(query);
    }
  }
}
