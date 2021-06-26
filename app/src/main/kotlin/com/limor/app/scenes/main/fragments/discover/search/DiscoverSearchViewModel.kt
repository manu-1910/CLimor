package com.limor.app.scenes.main.fragments.discover.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import repositories.search.SearchRepository
import timber.log.Timber
import javax.inject.Inject

class DiscoverSearchViewModel @Inject constructor(
    val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchResult = MutableLiveData<SearchResult>()
    val searchResult: LiveData<SearchResult> = _searchResult

    private var searchJob: Job? = null

    fun search(searchQuery: String, selectedTab: DiscoverSearchFragment.Tab) {
        Timber.d("Search for: $searchQuery")
        searchJob?.cancel()
        searchJob = viewModelScope.launch {

            // TODO replace with repo call
            delay((150L..500L).random())
            _searchResult.value = when (selectedTab) {
                DiscoverSearchFragment.Tab.ACCOUNTS -> SearchResult.Accounts(getRandomAccountResult())
                DiscoverSearchFragment.Tab.CATEGORIES -> SearchResult.Categories(getRandomCategories())
                DiscoverSearchFragment.Tab.HASHTAGS -> SearchResult.Hashtags(getRandomHashtags())
            }
        }
    }

    private fun getRandomAccountResult(): List<MockPerson> {
        val list = listOf(
            MockPerson(
                name = "Test Person",
                nickName = "@testPerson",
                imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-demo-photo-0c.jpg",
                isFollowed = false
            ),
            MockPerson(
                name = "Test Person2",
                nickName = "@testPerson2",
                imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/04.jpg",
                isFollowed = true
            ),
            MockPerson(
                name = "Test Person3",
                nickName = "@testPerson3",
                imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/08.jpg",
                isFollowed = true
            ),
            MockPerson(
                name = "Test Person4",
                nickName = "@testPerson4",
                imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/05.jpg",
                isFollowed = true
            ),
            MockPerson(
                name = "Test Person5",
                nickName = "@testPerson5",
                imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/06.jpg",
                isFollowed = false
            ),
        )

        // 1..5 random elements from the list
        return mutableListOf<MockPerson>().apply {
            repeat((1..5).random()) {
                add(list[(list.indices).random()])
            }
        }
    }

    private fun getRandomCategories(): List<String> {
        val list = listOf("Sport", "Leisure", "Tourism", "Books", "Sea", "42")
        // 1..5 random elements from the list
        return mutableListOf<String>().apply {
            repeat((1..5).random()) {
                add(list[(list.indices).random()])
            }
        }
    }

    private fun getRandomHashtags(): List<String> {
        val list = listOf("#Sport", "#Leisure", "#Tourism", "#Books", "#Sea", "#42")
        // 1..5 random elements from the list
        return mutableListOf<String>().apply {
            repeat((1..5).random()) {
                add(list[(list.indices).random()])
            }
        }
    }

    sealed class SearchResult {
        data class Accounts(val resultList: List<MockPerson>) : SearchResult()
        data class Categories(val resultList: List<String>) : SearchResult()
        data class Hashtags(val resultList: List<String>) : SearchResult()
    }
}