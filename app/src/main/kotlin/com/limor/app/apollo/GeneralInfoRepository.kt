package com.limor.app.apollo

import com.limor.app.*
import timber.log.Timber
import javax.inject.Inject

class GeneralInfoRepository @Inject constructor(val apollo: Apollo) {

    suspend fun fetchCategories(): List<CategoriesQuery.Category>? {
        val query = CategoriesQuery()
        val result = apollo.launchQuery(query)
        var categories: List<CategoriesQuery.Category?>? =
            result?.data?.categories ?: return null
        categories = categories!!.filterNotNull()
        logList(categories)
        return categories
    }

    suspend fun fetchLanguages(): List<LanguagesQuery.Language>? {
        val query = LanguagesQuery()
        val result = apollo.launchQuery(query)
        var languages: List<LanguagesQuery.Language?>? =
            result?.data?.languages ?: return null
        languages = languages!!.filterNotNull()
        logList(languages)
        return languages
    }

    suspend fun fetchGenders(): List<GendersQuery.Gender>? {
        val query = GendersQuery()
        val result = apollo.launchQuery(query)
        var genders: List<GendersQuery.Gender?>? =
            result?.data?.genders ?: return null
        genders = genders!!.filterNotNull()
        logList(genders)
        return genders
    }

    suspend fun fetchHomeFeed(): List<FeedItemsQuery.FeedItem>? {
        val query = FeedItemsQuery()
        val result = apollo.launchQuery(query)
        var feedItems: List<FeedItemsQuery.FeedItem?> =
            result?.data?.feedItems ?: return null
        feedItems = feedItems.filterNotNull()
        logList(feedItems)
        return feedItems
    }

    private fun logList(list: List<Any>) {
        if (!BuildConfig.DEBUG) return
        list.forEach {
            Timber.d(it.toString())
        }
    }
}