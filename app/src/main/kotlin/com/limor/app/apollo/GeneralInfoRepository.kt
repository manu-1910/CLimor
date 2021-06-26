package com.limor.app.apollo

import com.limor.app.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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


    suspend fun getUserProfile(): GetUserProfileQuery.GetUser? {
        val query = GetUserProfileQuery()
        val queryResult = withContext(Dispatchers.IO){
            apollo.launchQuery(query)
        }
        val createUserResult: GetUserProfileQuery.GetUser =
            queryResult?.data?.getUser ?: return null
        Timber.d("Got User -> ${createUserResult.username}")
        return createUserResult
    }

    suspend fun getBlockedUsers(): ArrayList<GetBlockedUsersQuery.GetBlockedUser?>? {
        val query = GetBlockedUsersQuery(10,10)
        val queryResult = withContext(Dispatchers.IO){
            apollo.launchQuery(query)
        }
        val createUserResult: List<GetBlockedUsersQuery.GetBlockedUser?> =
            queryResult?.data?.getBlockedUsers ?: return null
        Timber.d("Got Blocked Users -> ${createUserResult.size}")
        return createUserResult as ArrayList<GetBlockedUsersQuery.GetBlockedUser?>
    }

    suspend fun getFollowers(limit:Int,offset:Int): List<FollowersQuery.GetFollower?>? {
        val query = FollowersQuery(10,10)
        val queryResult = withContext(Dispatchers.IO){
            apollo.launchQuery(query)
        }
        val createUserResult: List<FollowersQuery.GetFollower?> =
            queryResult?.data?.getFollowers ?: return null
        Timber.d("Got FF -> ${createUserResult.size}")
        return createUserResult
    }
}