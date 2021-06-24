package com.limor.app.scenes.main.fragments.profile

import androidx.lifecycle.ViewModel
import com.limor.app.FollowersQuery
import com.limor.app.apollo.Apollo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class FollowViewModelNew: ViewModel() {


    suspend fun getFollowers(): List<FollowersQuery.GetFollower?>? {
        val query = FollowersQuery(10,10)
        val queryResult = withContext(Dispatchers.IO){
            Apollo.launchQuery(query)
        }
        val createUserResult: List<FollowersQuery.GetFollower?>? =
            queryResult?.data?.getFollowers
        Timber.d("Got Followers -> ${createUserResult?.size}")
        return createUserResult
    }


}