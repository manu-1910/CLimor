package com.limor.app.scenes.main.fragments.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.FollowersQuery
import com.limor.app.FriendsQuery
import com.limor.app.GetBlockedUsersQuery
import com.limor.app.GetUserProfileQuery
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.model.UserInfoProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository,
    val userInfoProvider: UserInfoProvider,
) : ViewModel() {
    private var _settingsToolBarTitle =
        MutableLiveData<String>()
    val settingsToolBarTitle: LiveData<String>
        get() = _settingsToolBarTitle

    var blockedUsersLimit: Int = 16
    var blockUsersOffset: Int = 0
    private var _blockedUsersData =
        MutableLiveData<List<GetBlockedUsersQuery.GetBlockedUser?>>()
    val blockedUsersData: LiveData<List<GetBlockedUsersQuery.GetBlockedUser?>>
        get() = _blockedUsersData
    private var _blockedUserErrorLiveData =
        MutableLiveData<String>()
    val blockedUserErrorLiveData: LiveData<String>
        get() = _blockedUserErrorLiveData
    private var _userInfoLiveData =
        MutableLiveData<GetUserProfileQuery.GetUser?>()
    val userInfoLiveData: LiveData<GetUserProfileQuery.GetUser?>
        get() = _userInfoLiveData

    private var _userUpdatedResponse =
        MutableLiveData<String?>()
    val userUpdatedResponse: LiveData<String?>
        get() = _userUpdatedResponse


    private var _followersData =
        MutableLiveData<List<FollowersQuery.GetFollower?>?>()
    val followersData: LiveData<List<FollowersQuery.GetFollower?>?>
        get() = _followersData

    private var _followingsData =
        MutableLiveData<List<FriendsQuery.GetFriend?>?>()
    val followingsData: LiveData<List<FriendsQuery.GetFriend?>?>
        get() = _followingsData

    fun getBlockedUsers(offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getBlockedUsers(blockedUsersLimit,offset)
                }
                _blockedUsersData.postValue(blockedUsers!!)
                Timber.d("Got Blocked -> $blockedUsers")
            } catch (e: Exception) {
                Timber.d("Got Blocked -> $e")
                _blockedUserErrorLiveData.postValue(e.localizedMessage)
            }
        }
        viewModelScope.launch {
            try {


            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun clearBlockedUsers() {
        _blockedUsersData.postValue(ArrayList())
    }

    fun getFollowers(offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getFollowers(blockedUsersLimit, offset)
                }
                _followersData.postValue(blockedUsers)
                Timber.d("Got Follow -> $blockedUsers")
            } catch (e: Exception) {
                Timber.d("Got Follow -> $e")
            }
        }
    }

    fun getFollowings(offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getFollowings(blockedUsersLimit, offset)
                }
                _followingsData.postValue(blockedUsers)
                Timber.d("Got Follow -> $blockedUsers")
            } catch (e: Exception) {
                Timber.d("Got Follow -> $e")
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val userInfo = withContext(Dispatchers.IO) {
                    generalInfoRepository.getUserProfile()
                }
                _userInfoLiveData.postValue(userInfo)
                Timber.d("Got UserInfo -> $userInfo")
            } catch (e: Exception) {
                Timber.d("Got UserInfo -> $e")
            }
        }

    }

    fun updateUserInfo(
        userName: String,
        firstName: String,
        lastName: String,
        bio: String,
        website: String,
        imageURL: String?
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.IO) {
                    userInfoProvider.updateUserProfile(userName, firstName, lastName, bio, website, imageURL)
                }
                _userUpdatedResponse.postValue(response)
                Timber.d("Updated UserInfo -> $response")
            } catch (e: Exception) {
                _userUpdatedResponse.postValue("Unable to Update Now")
                Timber.d("Updated UserInfo -> $e")
            }
        }
    }

    fun clearFollowers() {
        _followersData.postValue(ArrayList())
    }

    fun clearFollowing() {
        _followingsData.postValue(ArrayList())
    }

    fun followUser(id: Int) {
        viewModelScope.launch {
            userInfoProvider.startFollowingUser(id)
        }
    }

    fun unFollowUser(id: Int) {
        viewModelScope.launch {
            userInfoProvider.unFollowUser(id)
        }
    }

    fun setToolbarTitle(title: String) {
        _settingsToolBarTitle.value = title
         clearBlockedUsers()
    }

    fun createBlockedUser(id: Int) {
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