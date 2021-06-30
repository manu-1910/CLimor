package com.limor.app.scenes.main.fragments.discover.search.list

import com.limor.app.scenes.main.fragments.discover.search.DiscoverSearchViewModel.SearchResult
import com.limor.app.scenes.main.fragments.discover.search.list.item.AccountSearchItem
import com.limor.app.scenes.main.fragments.discover.search.list.item.CategorySearchItem
import com.limor.app.scenes.main.fragments.discover.search.list.item.HashtagSearchItem
import com.xwray.groupie.GroupieAdapter

class DiscoverSearchAdapter : GroupieAdapter() {

    fun updateSearchResult(result: SearchResult) {
        when (result) {
            is SearchResult.Accounts -> onAccountSearchResults(result)
            is SearchResult.Categories -> onCategorySearchResults(result)
            is SearchResult.Hashtags -> onHashtagSearchResults(result)
            else -> throw IllegalArgumentException()
        }
    }

    private fun onCategorySearchResults(result: SearchResult.Categories) {
        clear()
        addAll(
            result.resultList.map { categoryResult ->
                CategorySearchItem(categoryResult)
            }
        )
    }

    private fun onAccountSearchResults(result: SearchResult.Accounts) {
        clear()
        addAll(
            result.resultList.map { accountResult ->
                AccountSearchItem(accountResult)
            }
        )
    }

    private fun onHashtagSearchResults(result: SearchResult.Hashtags) {
        clear()
        addAll(
            result.resultList.map { hashtagResult ->
                HashtagSearchItem(hashtagResult)
            }
        )
    }
}