package com.limor.app.scenes.main.fragments.profile

import androidx.lifecycle.ViewModel
import com.limor.app.FollowersQuery
import com.limor.app.apollo.Apollo
import com.limor.app.apollo.GeneralInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class FollowViewModelNew @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository
): ViewModel() {


/*    suspend fun getFollowers(): List<FollowersQuery.GetFollower?>? {
        return generalInfoRepository.getFollowers()
    }*/


}