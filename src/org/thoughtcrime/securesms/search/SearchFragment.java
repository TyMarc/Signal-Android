package org.thoughtcrime.securesms.search;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.search.model.SearchResult;
import org.thoughtcrime.securesms.util.StickyHeaderDecoration;

import java.util.concurrent.Executors;

/**
 * A fragment that is displayed to do full-text search of messages, groups, and contacts.
 */
public class SearchFragment extends Fragment {

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

    listAdapter = new SearchListAdapter(GlideApp.with(this));
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

  public void updateSearchQuery(@NonNull String query) {
    Log.e(TAG, "query: " + query);
    if (viewModel != null) {
      viewModel.updateQuery(query);
    }
  }
}
