package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FeedItemsQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.GetUserProfileQuery
import com.limor.app.apollo.Apollo
import com.limor.app.apollo.GeneralInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class HomeFeedViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository
) : ViewModel() {

    private var _homeFeedLiveData =
        MutableLiveData<List<FeedItemsQuery.FeedItem>?>()
    val homeFeedLiveData: LiveData<List<FeedItemsQuery.FeedItem>?>
        get() = _homeFeedLiveData

    private var _homeFeedErrorLiveData =
        MutableLiveData<String>()
    val homeFeedErrorLiveData: LiveData<String>
        get() = _homeFeedErrorLiveData

    private var _userProfileData =
        MutableLiveData<GetUserProfileQuery.GetUser?>()
    val userProfileData: LiveData<GetUserProfileQuery.GetUser?>
        get() = _userProfileData

    fun loadHomeFeed() {
        viewModelScope.launch {
            try {
                val feedItems = generalInfoRepository.fetchHomeFeed()
                _homeFeedLiveData.postValue(feedItems)
            } catch (e: Exception) {
                _homeFeedErrorLiveData.postValue(e.localizedMessage)
            }

        }
    }


    fun getUserProfile(){
        viewModelScope.launch {
            try {
                val user = generalInfoRepository.getUserProfile()
                _userProfileData.postValue(user)
            } catch (e: Exception) {

            }
        }
    }

    suspend fun getBlockedUsers(): ArrayList<GetBlockedUsersQuery.GetBlockedUser?>? {
        return generalInfoRepository.getBlockedUsers()
    }

}