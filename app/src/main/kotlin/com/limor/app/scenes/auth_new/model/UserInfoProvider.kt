package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.limor.app.R
import com.limor.app.apollo.UserRepository
import com.limor.app.apollo.interceptors.AuthInterceptor
import com.limor.app.scenes.auth_new.data.DobInfo.Companion.parseForUserCreation
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.JwtChecker
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


class UserInfoProvider @Inject constructor(
    val userRepository: UserRepository,
    val phoneAuthHandler: PhoneAuthHandler
) {


    private val _breakPointLiveData = MutableLiveData<String?>().apply { value = null }
    val breakPointLiveData: LiveData<String?>
        get() = _breakPointLiveData

    private val _createUserLiveData = MutableLiveData<String?>().apply { value = null }
    val createUserLiveData: LiveData<String?>
        get() = _createUserLiveData

    private val _updateUserNameLiveData = MutableLiveData<String?>().apply { value = null }
    val updateUserNameLiveData: LiveData<String?>
        get() = _updateUserNameLiveData

    private val _updateUserDOBLiveData = MutableLiveData<String?>().apply { value = null }
    val updateUserDOBLiveData: LiveData<String?>
        get() = _updateUserDOBLiveData

    private val _updatePreferredInfoLiveData = MutableLiveData<String?>().apply { value = null }
    val updatePreferredInfoLiveData: LiveData<String?>
        get() = _updatePreferredInfoLiveData

    private val _updateOnboardingStatusLiveData = MutableLiveData<String?>().apply { value = null }
    val updateOnboardingStatusLiveData: LiveData<String?>
        get() = _updateOnboardingStatusLiveData

    private val _userInfoProviderErrorLiveData = MutableLiveData<Any?>().apply { value = null }
    val userInfoProviderErrorLiveData: LiveData<Any?>
        get() = _userInfoProviderErrorLiveData

    @ExperimentalCoroutinesApi
    fun getUserOnboardingStatus(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            try {
                val response = userRepository.getUserOnboardingStatus() ?: ""
                val breakpoint = getBreakpointAccordingToEmailPresence(response)
                _breakPointLiveData.postValue(breakpoint)
                createDeviceToken().collect {
                    userRepository.createUserDevice(it)
                }
                delay(500)
                _breakPointLiveData.postValue(null)
            } catch (e: Exception) {
                e.printStackTrace()
                _breakPointLiveData.postValue(null)
                _userInfoProviderErrorLiveData.postValue(e.message)
                delay(500)
                _userInfoProviderErrorLiveData.postValue(null)
            }
        }
    }

    private fun getBreakpointAccordingToEmailPresence(response: String): String {
        //check if user has an email on it's JWT
        val jwt = AuthInterceptor.getToken()
        val hasEmail = JwtChecker.isJwtContainsEmail(jwt)
        return if (hasEmail) response else NavigationBreakpoints.ACCOUNT_CREATION.destination

    }

    fun updateDOB(scope: CoroutineScope, dob: Long) {
        if (dob == 0L) {
            return
        }
        scope.launch {
            try {
                val formattedDate = parseForUserCreation(dob)
                Timber.d("Formatted DOB $formattedDate")
                val response = userRepository.updateUserDOB(formattedDate) ?: ""
                _updateUserDOBLiveData.postValue(response)
            } catch (e: Exception) {
                _userInfoProviderErrorLiveData.postValue(e.message)
                delay(500)
                _userInfoProviderErrorLiveData.postValue(null)
            }
        }
    }

    fun createUser(scope: CoroutineScope, dob: Long) {
        if (dob == 0L) return
        scope.launch {
            if (dob == 0L) {
                //This is possible if user Signing in, but did not create Limor account before (doesn't have "luid" in JWT)
                _userInfoProviderErrorLiveData.postValue(R.string.no_user_found_offer_to_sign_up)
                delay(500)
                _userInfoProviderErrorLiveData.postValue(null)
                return@launch
            }

            try {
                val formattedDate = parseForUserCreation(dob)
                Timber.d("Formatted DOB $formattedDate")
                val response = userRepository.createUser(formattedDate) ?: ""
                if(response == "Success"){
                    createDeviceToken().collect {
                        userRepository.createUserDevice(it)
                    }
                }

                _createUserLiveData.postValue(response)
                delay(500)
                _createUserLiveData.postValue(null)
            } catch (e: Exception) {
                _userInfoProviderErrorLiveData.postValue(e.message)
                delay(500)
                _userInfoProviderErrorLiveData.postValue(null)
            }

        }
    }

    @ExperimentalCoroutinesApi
    suspend fun createDeviceToken() =  callbackFlow<String>{
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            token ->
            trySend(token)
            close()
        }
        awaitClose()
    }

    fun updateUserName(scope: CoroutineScope, userName: String) {
        scope.launch(Dispatchers.Default) {
            try {
                val response = userRepository.updateUserName(userName)
                _updateUserNameLiveData.postValue(response)
                delay(500)
                _updateUserNameLiveData.postValue(null)
            } catch (e: Exception) {
                _userInfoProviderErrorLiveData.postValue(e.message)
                delay(500)
                _userInfoProviderErrorLiveData.postValue(null)
            }
        }
    }

    private val _userNameAttachedToUserLiveData = MutableLiveData<Boolean?>().apply { value = null }
    val userNameAttachedToUserLiveData: LiveData<Boolean?>
        get() = _userNameAttachedToUserLiveData

    fun updateFirebaseUserName(scope: CoroutineScope, userName: String) {
        scope.launch(Dispatchers.Default) {
            withContext(Dispatchers.IO) {
                updateFirebaseUserNameScoped(userName)
            }
        }
    }

    private suspend fun updateFirebaseUserNameScoped(userName: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()

        val task = user.updateProfile(profileUpdates)
        try {
            val result = Tasks.await(task)
            _userNameAttachedToUserLiveData.postValue(true)
            delay(500)
            _userNameAttachedToUserLiveData.postValue(null)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Timber.e(e)
            try {
                phoneAuthHandler.reAuthWithPhoneCredential()
            } catch (e: Exception) {
                Timber.e(e)
                _userInfoProviderErrorLiveData.postValue(e.cause?.message ?: e.message)
            }
        } catch (e: Exception) {
            Timber.e(e)
            _userInfoProviderErrorLiveData.postValue(e.cause?.message ?: e.message)
        }
    }

    fun updatePreferredInfo(
        scope: CoroutineScope,
        gender: Int,
        categories: List<Int?>,
        languages: List<String?>
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                val result = userRepository.updateUserOnboardingData(gender, categories, languages)
                _updatePreferredInfoLiveData.postValue(result)
                delay(500)
                _updatePreferredInfoLiveData.postValue(null)
            } catch (e: Exception) {
                _userInfoProviderErrorLiveData.postValue(e.cause?.message ?: e.message)
                delay(500)
                _userInfoProviderErrorLiveData.postValue(null)
            }
        }
    }

    fun updateUserOnboardingStatus(scope: CoroutineScope, nextStep: String) {
        scope.launch(Dispatchers.Default) {
            try {
                val result = userRepository.updateUserOnboardingStatus(nextStep)
                _updateOnboardingStatusLiveData.postValue(result)
                delay(400)
                _updateOnboardingStatusLiveData.postValue(null)
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        fun userNameRegExCheck(userName: String): Boolean {
            val pattern: Pattern =
                Pattern.compile("^(?=.{3,30}\$)(?![_.0-9])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])\$")
            val matcher: Matcher = pattern.matcher(userName)
            while (matcher.find()) {
                return true
            }
            return false
        }
    }


    suspend fun updateUserProfile(
        userName: String,
        firstName: String,
        lastName: String,
        bio: String,
        website: String,
        imageURL: String?,
        voiceBioURL: String?,
        durationSeconds: Double?
    ) :String? {
       return userRepository.updateUserProfile(
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

    suspend fun startFollowingUser(id: Int) {
        try {
            userRepository.startFollowingUser(id)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun unFollowUser(id: Int) {
        try {
            userRepository.unFollowUser(id)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun blockUser(id: Int) {
        try {
            userRepository.blockUser(id)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
    suspend fun unblockUser(id: Int) {
        try {
            userRepository.unblockUser(id)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun reportUser(id: Int,reason:String) {
        try {
            userRepository.reportUser(id,reason)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}