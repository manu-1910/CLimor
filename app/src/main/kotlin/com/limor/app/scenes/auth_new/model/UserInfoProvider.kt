package com.limor.app.scenes.auth_new.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.limor.app.apollo.UserRepository
import com.limor.app.apollo.interceptors.AuthInterceptor
import com.limor.app.scenes.auth_new.data.DobInfo.Companion.parseForUserCreation
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.JwtChecker
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.regex.Matcher
import java.util.regex.Pattern


class UserInfoProvider(private val scope: CoroutineScope) {


    private val _breakPointLiveData = MutableLiveData<String?>().apply { value = null }
    val breakPointLiveData: LiveData<String?>
        get() = _breakPointLiveData

    private val _createUserLiveData = MutableLiveData<String?>().apply { value = null }
    val createUserLiveData: LiveData<String?>
        get() = _createUserLiveData

    private val _updateUserNameLiveData = MutableLiveData<String?>().apply { value = null }
    val updateUserNameLiveData: LiveData<String?>
        get() = _updateUserNameLiveData

    private val _updatePreferredInfoLiveData = MutableLiveData<String?>().apply { value = null }
    val updatePreferredInfoLiveData: LiveData<String?>
        get() = _updatePreferredInfoLiveData

    private val _updateOnboardingStatusLiveData = MutableLiveData<String?>().apply { value = null }
    val updateOnboardingStatusLiveData: LiveData<String?>
        get() = _updateOnboardingStatusLiveData

    private val _userInfoProviderErrorLiveData = MutableLiveData<String?>().apply { value = null }
    val userInfoProviderErrorLiveData: LiveData<String?>
        get() = _userInfoProviderErrorLiveData

    fun getUserOnboardingStatus() {
        scope.launch(Dispatchers.Default) {
            try {
                val response = UserRepository.getUserOnboardingStatus() ?: ""
                val breakpoint = getBreakpointAccordingToEmailPresence(response)
                _breakPointLiveData.postValue(breakpoint)
                delay(500)
                _breakPointLiveData.postValue(null)
            } catch (e: Exception) {
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

    fun createUser(dob: Long) {
        if (dob == 0L) return
        scope.launch {
            try {
                val formattedDate = parseForUserCreation(dob)
                Timber.d("Formatted DOB $formattedDate")
                val response = UserRepository.createUser(formattedDate) ?: ""
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

    fun updateUserName(userName: String) {
        scope.launch(Dispatchers.Default) {
            try {
                val response = UserRepository.updateUserName(userName)
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

    fun updateFirebaseUserName(userName: String) {
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
                PhoneAuthHandler.reAuthWithPhoneCredential()
            } catch (e: Exception) {
                Timber.e(e)
                _userInfoProviderErrorLiveData.postValue(e.cause?.message ?: e.message)
            }
        } catch (e: Exception) {
            Timber.e(e)
            _userInfoProviderErrorLiveData.postValue(e.cause?.message ?: e.message)
        }
    }

    fun updatePreferredInfo(gender: Int, categories: List<Int?>, languages: List<String?>) {
        scope.launch(Dispatchers.Default) {
            try {
                val result = UserRepository.updateUserOnboardingData(gender, categories, languages)
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

    fun updateUserOnboardingStatus(nextStep: String) {
        scope.launch(Dispatchers.Default) {
            try {
                val result = UserRepository.updateUserOnboardingStatus(nextStep)
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
}