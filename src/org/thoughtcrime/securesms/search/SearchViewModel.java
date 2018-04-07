package org.thoughtcrime.securesms.search;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.search.model.SearchResult;

/**
 * A {@link ViewModel} for handling all the business logic and interactions that take place inside
 * of the {@link SearchFragment}.
 *
 * This class should be view- and Android-agnostic, and therefore should contain no references to
 * things like {@link android.content.Context}, {@link android.view.View},
 * {@link android.support.v4.app.Fragment}, etc.
 */
public class SearchViewModel extends ViewModel {

  private final MutableLiveData<SearchResult> searchResult;
  private final SearchRepository              searchRepository;

  public SearchViewModel(@NonNull SearchRepository searchRepository) {
    this.searchResult     = new MutableLiveData<>();
    this.searchRepository = searchRepository;
  }

  public LiveData<SearchResult> getSearchResult() {
    return searchResult;
  }

  public void updateQuery(String query) {
    // TODO: Throttling
    searchRepository.query(query, searchResult::postValue);
  }

  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final SearchRepository searchRepository;

    public Factory(@NonNull SearchRepository searchRepository) {
      this.searchRepository = searchRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return modelClass.cast(new SearchViewModel(searchRepository));
    }
  }
}
