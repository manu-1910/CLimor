package com.limor.app.apollo

import com.limor.app.*
import com.limor.app.uimodels.UserUIModel
import com.limor.app.uimodels.mapToUIModel
import com.limor.app.apollo.Apollo.Companion.LOAD_PORTION
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

    suspend fun fetchHomeFeed(
        limit: Int = LOAD_PORTION,
        offset: Int = 0
    ): List<FeedItemsQuery.GetFeedItem> {
        val query = FeedItemsQuery(limit, offset)
        val result = apollo.launchQuery(query)
        var feedItems: List<FeedItemsQuery.GetFeedItem?> =
            result?.data?.getFeedItems ?: return emptyList()
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

    suspend fun getUserProfile(): UserUIModel? {
        val query = GetUserProfileQuery()
        val queryResult = withContext(Dispatchers.IO) {
            apollo.launchQuery(query)
        }
        val createUserResult: GetUserProfileQuery.GetUser =
            queryResult?.data?.getUser ?: return null
        Timber.d("Got User -> ${createUserResult.username}")
        return createUserResult.mapToUIModel()
    }

    suspend fun getUserProfileById(id: Int): UserUIModel? {
        val query = GetUserProfileByIdQuery(id)
        val queryResult = withContext(Dispatchers.IO){
            apollo.launchQuery(query)
        }
        val createUserResult: GetUserProfileByIdQuery.GetUserById =
            queryResult?.data?.getUserById ?: return null
        Timber.d("Got User -> ${createUserResult.username}  ${createUserResult.id}  ")
        return createUserResult.mapToUIModel()
    }

    suspend fun getBlockedUsers(limit:Int,offset:Int): List<GetBlockedUsersQuery.GetBlockedUser?>? {
        val query = GetBlockedUsersQuery(limit,offset)
        val queryResult = withContext(Dispatchers.IO) {
            apollo.launchQuery(query)
        }
        val createUserResult: List<GetBlockedUsersQuery.GetBlockedUser?> =
            queryResult?.data?.getBlockedUsers ?: return null
        Timber.d("Got Blocked Users -> ${createUserResult.size}")
        return createUserResult
    }

    suspend fun getFollowers(limit:Int,offset:Int): List<FollowersQuery.GetFollower?>? {
        val query = FollowersQuery(limit,offset)
        val queryResult = withContext(Dispatchers.IO){
            apollo.launchQuery(query)
        }
        val createUserResult: List<FollowersQuery.GetFollower?> =
            queryResult?.data?.getFollowers ?: return null
        Timber.d("Got FF -> ${createUserResult.size}")
        return createUserResult
    }
    suspend fun getFollowings(limit:Int,offset:Int): List<FriendsQuery.GetFriend?>? {
        val query = FriendsQuery(limit,offset)
        val queryResult = withContext(Dispatchers.IO){
            apollo.launchQuery(query)
        }
        val createUserResult: List<FriendsQuery.GetFriend?> =
            queryResult?.data?.getFriends?: return null
        Timber.d("Got FF -> ${createUserResult.size}")
        return createUserResult
    }

    suspend fun getSuggestedPeople(): List<SuggestedPeopleQuery.GetSuggestedUser>? {
        return apollo.launchQuery(SuggestedPeopleQuery())
            ?.data?.getSuggestedUsers?.filterNotNull()?.also {
                Timber.d("getSuggestedPeople() -> $it")
            }
    }
}