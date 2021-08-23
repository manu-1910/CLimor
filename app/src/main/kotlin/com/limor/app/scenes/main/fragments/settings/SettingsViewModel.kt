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
import com.limor.app.uimodels.UserUIModel
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

    var blockedUsersLimit: Int = -1
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
        MutableLiveData<UserUIModel?>()
    val userInfoLiveData: LiveData<UserUIModel?>
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
        MutableLiveData<List<FriendsQuery.GetFriend?>>()
    val followingsData: LiveData<List<FriendsQuery.GetFriend?>>
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

    fun getFollowers(userId:Int?,offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getFollowers(userId,blockedUsersLimit, offset)
                }
                _followersData.postValue(blockedUsers)
                Timber.d("Got Follow -> $blockedUsers")
            } catch (e: Exception) {
                Timber.d("Got Follow -> $e")
            }
        }
    }

    fun getFollowings(userId:Int?,offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getFollowings(userId,blockedUsersLimit, offset)
                }
                blockedUsers?.let{
                    _followingsData.postValue(it)
                }
                Timber.d("Got Following -> $blockedUsers $offset")
            } catch (e: Exception) {
                Timber.d("Got Following -> $e")
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val userInfo = withContext(Dispatchers.IO) {
                    generalInfoRepository.getUserProfile()
                }
                userInfo?.let{
                    _userInfoLiveData.postValue(it)
                }
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
        println("UPdating user image to $imageURL")
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.IO) {
                    userInfoProvider.updateUserProfile(userName, firstName, lastName, bio, website, imageURL)
                }
                _userUpdatedResponse.postValue(USER_UPDATE_SUCCESS)
                Timber.d("Updated UserInfo -> $response")
            } catch (e: Exception) {
                _userUpdatedResponse.postValue(USER_UPDATE_FAILURE)
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

    companion object {
        const val USER_UPDATE_SUCCESS = "Success"
        const val USER_UPDATE_FAILURE = "Failure"
    }
}