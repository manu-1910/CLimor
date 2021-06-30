package com.limor.app.scenes.main.fragments.discover.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.main.fragments.discover.search.DiscoverSearchFragment.Tab
import com.limor.app.uimodels.CategoryUIModel
import com.limor.app.uimodels.TagUIModel
import com.limor.app.uimodels.UserUIModel
import com.limor.app.usecases.SearchCategoriesUseCase
import com.limor.app.usecases.SearchHashtagsUseCase
import com.limor.app.usecases.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DiscoverSearchViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val searchCategoriesUseCase: SearchCategoriesUseCase,
    private val searchHashtagsUseCase: SearchHashtagsUseCase,
) : ViewModel() {

    private val _searchResult = MutableLiveData<SearchResult>()
    val searchResult: LiveData<SearchResult> = _searchResult

    private var searchJob: Job? = null

    fun search(searchQuery: String, selectedTab: Tab) {
        Timber.d("Search for: $searchQuery")
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            when (selectedTab) {
                Tab.ACCOUNTS -> {
                    searchUsersUseCase.execute(searchQuery)
                        .onSuccess { _searchResult.value = SearchResult.Accounts(it) }
                        .onFailure { Timber.e(it, "Error while searching for users") }
                }
                Tab.CATEGORIES -> {
                    searchCategoriesUseCase.execute(searchQuery)
                        .onSuccess { _searchResult.value = SearchResult.Categories(it) }
                        .onFailure { Timber.e(it, "Error while searching for categories") }
                }
                Tab.HASHTAGS -> {
                    searchHashtagsUseCase.execute(searchQuery)
                        .onSuccess { _searchResult.value = SearchResult.Hashtags(it) }
                        .onFailure { Timber.e(it, "Error while searching for hashtags") }
                }
            }
        }
    }

    sealed class SearchResult {
        data class Accounts(val resultList: List<UserUIModel>) : SearchResult()
        data class Categories(val resultList: List<CategoryUIModel>) : SearchResult()
        data class Hashtags(val resultList: List<TagUIModel>) : SearchResult()
    }
}