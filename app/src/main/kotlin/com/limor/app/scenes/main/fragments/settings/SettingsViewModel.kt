package com.limor.app.scenes.main.fragments.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FollowersQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.GetUserProfileQuery
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.firebase.EmailAuthHandler
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler
import com.limor.app.scenes.auth_new.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository,
    val userInfoProvider: UserInfoProvider,
) : ViewModel() {
    var blockedUsersLimit: Int = 16
    var blockUsersOffset: Int = 0
    private var _blockedUsersData =
        MutableLiveData<ArrayList<GetBlockedUsersQuery.GetBlockedUser?>>()
    val blockedUsersData: LiveData<ArrayList<GetBlockedUsersQuery.GetBlockedUser?>>
        get() = _blockedUsersData

    private var _followersData =
        MutableLiveData<List<FollowersQuery.GetFollower?>?>()
    val followersData: LiveData<List<FollowersQuery.GetFollower?>?>
        get() = _followersData

    fun getBlockedUsers() {
        viewModelScope.launch {
            try {
                val blockedUsers = generalInfoRepository.getBlockedUsers()
                _blockedUsersData.postValue(blockedUsers!!)
            } catch (e: Exception) {

            }
        }
    }

    fun clearBlockedUsers() {
        _blockedUsersData.postValue(ArrayList())
    }

    suspend fun getFollowers(offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO){
                    generalInfoRepository.getFollowers(blockedUsersLimit,offset)
                }
                _followersData.postValue(blockedUsers)
                Timber.d("Got Follow -> $blockedUsers")
            } catch (e: Exception) {
                Timber.d("Got Follow -> $e")
            }
        }
    }

    suspend fun getUserInfo(): GetUserProfileQuery.GetUser?{
        return try {
            withContext(Dispatchers.IO){
                generalInfoRepository.getUserProfile()
            }
        } catch (e: Exception) {
            Timber.d("Got Follow -> $e")
            null
        }
    }

    suspend fun updateUserInfo(userName: String, firstName:String, lastName:String, bio:String, website:String): String?{
        return userInfoProvider.updateUserProfile(userName,firstName,lastName,bio,website)
    }

    fun clearFollowers() {
        _followersData.postValue(ArrayList())
    }

}