package com.limor.app.scenes.main.fragments.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.*
import com.limor.app.apollo.Apollo
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.model.UserInfoProvider
import com.limor.app.scenes.auth_new.util.PrefsHandler
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
                Timber.d("User Data --> "+ id)
                val user = generalInfoRepository.getUserProfileById(id)
                Timber.d("User Data --> "+ user.toString())
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

    fun reportUser(reason: String, id: Int?) {
        id?.let{
            viewModelScope.launch {
                userInfoProvider.userRepository.reportUser(id,reason)
            }
        }
    }


    fun createDeviceToken(token: String) {
        viewModelScope.launch {
            userInfoProvider.userRepository.createUserDevice(token)
        }

    }

    fun testingRepo(token:String){
        Timber.d("working blindly...")
    }

    fun resetUser() {
        _userProfileData.value = null
    }

    fun requestPatronInvitation(userId: Int) {
        viewModelScope.launch {
            userInfoProvider.userRepository.requestPatronInvitation(userId)
        }
    }


}