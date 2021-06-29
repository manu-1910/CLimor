package com.limor.app.apollo

import com.limor.app.CategoriesQuery
import com.limor.app.SearchHashtagsQuery
import com.limor.app.SearchUsersQuery
import javax.inject.Inject

class SearchRepository @Inject constructor(private val apollo: Apollo) {

    suspend fun searchUsers(
        term: String,
        limit: Int = -1,
        offset: Int = 0
    ): List<SearchUsersQuery.SearchUser> {
        return apollo.launchQuery(
            SearchUsersQuery(
                term,
                limit,
                offset
            )
        )?.data?.searchUsers?.filterNotNull() ?: emptyList()
    }

    suspend fun searchHashtags(
        term: String,
        limit: Int = -1,
        offset: Int = 0
    ): List<SearchHashtagsQuery.SearchTag> {
        return apollo.launchQuery(
            SearchHashtagsQuery(
                term,
                limit,
                offset
            )
        )?.data?.searchTags?.filterNotNull() ?: emptyList()
    }

    suspend fun searchCategories(
        term: String
    ): List<CategoriesQuery.Category> {
        return apollo.launchQuery(
            CategoriesQuery()
        )
            ?.data
            ?.categories
            ?.filterNotNull()
            ?.filter {
                it.name != null && it.name.startsWith(term)
            } ?: emptyList()
    }
}