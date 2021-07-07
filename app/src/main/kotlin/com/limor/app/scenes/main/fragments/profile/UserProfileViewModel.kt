package com.limor.app.scenes.main.fragments.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FollowersQuery
import com.limor.app.GetUserProfileByIdQuery
import com.limor.app.GetUserProfileQuery
import com.limor.app.apollo.Apollo
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.model.UserInfoProvider
import com.limor.app.uimodels.UserUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository,
    val userInfoProvider: UserInfoProvider
): ViewModel() {

    private var _profileErrorLiveData =
        MutableLiveData<String>()
    val profileErrorLiveData: LiveData<String>
        get() = _profileErrorLiveData

    private var _userProfileData =
        MutableLiveData<UserUIModel?>()
    val userProfileData: LiveData<UserUIModel?>
        get() = _userProfileData

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
                _userProfileData.postValue(user)
            } catch (e: Exception) {
                _profileErrorLiveData.postValue(e.localizedMessage)
            }
        }
    }

    fun startFollowing(id: Int) {
        viewModelScope.launch {
            userInfoProvider.startFollowingUser(id)
        }
    }

    fun unFollow(id: Int) {
        viewModelScope.launch {
            userInfoProvider.unFollowUser(id)
        }
    }

    fun blockUser(id: Int) {
        viewModelScope.launch {
            userInfoProvider.blockUser(id)
        }
    }

    fun unblockUser(id: Int) {
        viewModelScope.launch {
            userInfoProvider.unblockUser(id)
        }
    }


}