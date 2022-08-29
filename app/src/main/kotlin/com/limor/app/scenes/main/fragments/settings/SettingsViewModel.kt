package com.limor.app.scenes.main.fragments.settings

import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.*
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.model.CountriesListProvider
import com.limor.app.scenes.auth_new.model.GendersProvider
import com.limor.app.scenes.auth_new.model.UserInfoProvider
import com.limor.app.scenes.auth_new.util.PhoneNumberChecker
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.UserUIModel
import com.limor.app.uimodels.mapToUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val generalInfoRepository: GeneralInfoRepository,
    val userInfoProvider: UserInfoProvider,
    val genderInfoProvider: GendersProvider
) : ViewModel() {
    private var _settingsToolBarTitle =
        MutableLiveData<String>()
    val settingsToolBarTitle: LiveData<String>
        get() = _settingsToolBarTitle

    private var _showLogoInToolBar =
        MutableLiveData<Boolean?>()
    val showLogoInToolBar: LiveData<Boolean?>
        get() = _showLogoInToolBar

    private var _showToolBar =
        MutableLiveData<Boolean?>()
    val showToolBar: LiveData<Boolean?>
        get() = _showToolBar

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
        MutableLiveData<List<UserUIModel?>?>()
    val followersData: LiveData<List<UserUIModel?>?>
        get() = _followersData

    private var _followingsData =
        MutableLiveData<List<UserUIModel?>>()
    val followingsData: LiveData<List<UserUIModel?>>
        get() = _followingsData

    var searchFollowersOffset: Int = 0
    private var _searchFollowersData =
        MutableLiveData<List<UserUIModel?>?>()
    val searchFollowersData: LiveData<List<UserUIModel?>?>
        get() = _searchFollowersData

    var searchFollowingOffset: Int = 0
    private var _searchFollowingsData =
        MutableLiveData<List<UserUIModel?>?>()
    val searchFollowingsData: LiveData<List<UserUIModel?>?>
        get() = _searchFollowingsData

    val gendersLiveData: LiveData<List<GendersQuery.Gender>>
        get() = genderInfoProvider.gendersLiveData

    val gendersLiveDataError: LiveData<String>
        get() = genderInfoProvider.gendersLiveDataError

    val selectedGenderIndex: Int
        get() = genderInfoProvider.selectedGenderIndex()

    val currentGenderId: Int
        get() = genderInfoProvider.selectedGenderId

    fun getBlockedUsers(offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getBlockedUsers(blockedUsersLimit, offset)
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

    fun getFollowers(userId: Int?, offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getFollowers(userId, blockedUsersLimit, offset)
                }
                val followers: List<UserUIModel?>? = blockedUsers?.map {
                    it?.mapToUIModel()
                }
                _followersData.postValue(followers)
                Timber.d("Got Follow -> $blockedUsers")
            } catch (e: Exception) {
                Timber.d("Got Follow -> $e")
            }
        }
    }

    fun getFollowings(userId: Int?, offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val blockedUsers = withContext(Dispatchers.IO) {
                    generalInfoRepository.getFollowings(userId, blockedUsersLimit, offset)
                }
                blockedUsers?.let {
                    val followingUsers = it.map {
                        it?.mapToUIModel()
                    }
                    _followingsData.postValue(followingUsers)
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
                userInfo?.let {
                    _userInfoLiveData.postValue(it)
                }
                Timber.d("Got UserInfo -> $userInfo")
            } catch (e: Exception) {
                Timber.d("Got UserInfo -> $e")
            }

        }

    }

    fun updateUserInfo(
        genderId: Int,
        userName: String,
        firstName: String,
        lastName: String,
        bio: String,
        website: String,
        imageURL: String?,
        voiceBioURL: String?,
        durationSeconds: Double?
    ) {
        if (BuildConfig.DEBUG) {
            println("Updating user -> image to $imageURL")
            println("Updating user -> voice bio with duration of $durationSeconds at $voiceBioURL")
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val response = withContext(Dispatchers.IO) {
                    userInfoProvider.updateUserProfile(
                        genderId,
                        userName,
                        firstName,
                        lastName,
                        bio,
                        website,
                        imageURL,
                        voiceBioURL,
                        durationSeconds
                    )
                }
                _userUpdatedResponse.postValue(USER_UPDATE_SUCCESS)
                Timber.d("Updated UserInfo -> $response")
            } catch (e: Exception) {
                _userUpdatedResponse.postValue(USER_UPDATE_FAILURE)
                Timber.d("Updated UserInfo -> $e")
            }
        }
    }

    fun selectGender(id: Int) {
        genderInfoProvider.selectedGenderId = id
    }

    fun searchFollowers(searchTerm: String, offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val followersResults = withContext(Dispatchers.IO) {
                    generalInfoRepository.searchFollowers(searchTerm, blockedUsersLimit, offset)
                }
                val followers: List<UserUIModel?> = followersResults.map {
                    it?.mapToUIModel()
                }
                _searchFollowersData.postValue(followers)
                Timber.d("Got Follow -> $followersResults")
            } catch (e: Exception) {
                Timber.d("Got Follow -> $e")
            }
        }
    }

    fun searchFollowings(searchTerm: String, offset: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val followingResults = withContext(Dispatchers.IO) {
                    generalInfoRepository.searchFollowing(searchTerm, blockedUsersLimit, offset)
                }
                followingResults.let {
                    val followingUsers = it.map {
                        it?.mapToUIModel()
                    }
                    _searchFollowingsData.postValue(followingUsers)
                }
                Timber.d("Got Following -> $followingResults $offset")
            } catch (e: Exception) {
                Timber.d("Got Following -> $e")
            }
        }
    }

    fun clearFollowers() {
        searchFollowersOffset = 0
        _followersData.postValue(ArrayList())
    }

    fun clearFollowing() {
        searchFollowingOffset = 0
        _followingsData.postValue(ArrayList())
    }

    fun removeSearchFollowersResults() {
        searchFollowersOffset = 0
        _searchFollowersData.postValue(null)
    }

    fun removeSearchFollowingResults() {
        searchFollowingOffset = 0
        _searchFollowingsData.postValue(null)
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

    fun showLogoInToolbar(show: Boolean){
        _showLogoInToolBar.value = show
        _showLogoInToolBar.value = null
    }

    fun showToolbar(show: Boolean){
        _showToolBar.value = show
        _showToolBar.value = null
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

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userInfoProvider.setNotificationsEnabled(enabled)
        }
    }

    fun deleteUserDevice(){
        viewModelScope.launch {
            userInfoProvider.deleteUserDevice()
        }
    }

    fun downloadGenders() = genderInfoProvider.downloadGenders(viewModelScope)

    companion object {
        const val USER_UPDATE_SUCCESS = "Success"
        const val USER_UPDATE_FAILURE = "Failure"
    }
}