package com.limor.app.scenes.main_new.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FeedItemsQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.GetUserProfileByIdQuery
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

    private val _homeFeedLiveData =
        MutableLiveData<List<FeedItemsQuery.FeedItem>?>()
    val homeFeedLiveData: LiveData<List<FeedItemsQuery.FeedItem>?>
        get() = _homeFeedLiveData

    private var _homeFeedErrorLiveData =
        MutableLiveData<String>()
    val homeFeedErrorLiveData: LiveData<String>
        get() = _homeFeedErrorLiveData

    private var _profileErrorLiveData =
        MutableLiveData<String>()
    val profileErrorLiveData: LiveData<String>
        get() = _profileErrorLiveData

    private var _userProfileData =
        MutableLiveData<GetUserProfileQuery.GetUser?>()
    val userProfileData: LiveData<GetUserProfileQuery.GetUser?>
        get() = _userProfileData

    private var _userProfileIdData =
        MutableLiveData<GetUserProfileByIdQuery.GetUserById?>()
    val userProfileIdData: LiveData<GetUserProfileByIdQuery.GetUserById?>
        get() = _userProfileIdData

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
                _profileErrorLiveData.postValue(e.localizedMessage)
            }
        }
    }

    fun getUserById(id: Int){
        viewModelScope.launch {
            try {
                val user = generalInfoRepository.getUserProfileById(id)
                _userProfileIdData.postValue(user)
            } catch (e: Exception) {
                _profileErrorLiveData.postValue(e.localizedMessage)
            }
        }
    }


}