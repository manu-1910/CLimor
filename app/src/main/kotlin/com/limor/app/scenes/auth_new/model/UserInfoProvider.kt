package com.limor.app.scenes.auth_new.model

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.limor.app.apollo.UserRepository
import com.limor.app.scenes.auth_new.data.DobInfo.Companion.parseForUserCreation
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler
import com.limor.app.scenes.utils.MAIN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _userInfoProviderErrorLiveData = MutableLiveData<String?>().apply { value = null }
    val userInfoProviderErrorLiveData: LiveData<String?>
        get() = _userInfoProviderErrorLiveData

    fun getUserOnboardingStatus() {
        scope.launch {
            try {
                val response = UserRepository.getUserOnboardingStatus() ?: ""
                _breakPointLiveData.postValue(response) //NavigationBreakpoints.HOME_FEED.destination
                Handler().postDelayed({ _breakPointLiveData.postValue(null) }, 500)
            } catch (e: Exception) {
                Timber.e(e)
                _userInfoProviderErrorLiveData.postValue(e.message)
                Handler().postDelayed({ _userInfoProviderErrorLiveData.postValue(null) }, 500)
            }
        }
    }

    fun createUser(dob: Long) {
        if (dob == 0L) return
        scope.launch {
            try {
                val formattedDate = parseForUserCreation(dob)
                Timber.d("Formatted DOB $formattedDate")
                val response = UserRepository.createUser(formattedDate) ?: ""
                _createUserLiveData.postValue(response) //NavigationBreakpoints.HOME_FEED.destination
                Handler().postDelayed({ _createUserLiveData.postValue(null) }, 500)
            } catch (e: Exception) {
                Timber.e(e)
                _userInfoProviderErrorLiveData.postValue(e.message)
                Handler().postDelayed({ _userInfoProviderErrorLiveData.postValue(null) }, 500)
            }
        }
    }

    fun updateUserName(userName: String) {
        scope.launch {
            try {
                val response = UserRepository.updateUserName(userName)
                _updateUserNameLiveData.postValue(response) //NavigationBreakpoints.HOME_FEED.destination
                Handler().postDelayed({ _updateUserNameLiveData.postValue(null) }, 500)
            } catch (e: Exception) {
                Timber.e(e)
                _userInfoProviderErrorLiveData.postValue(e.message)
                Handler().postDelayed({ _userInfoProviderErrorLiveData.postValue(null) }, 500)
            }
        }
    }

    private val _userNameAttachedToUserLiveData = MutableLiveData<Boolean?>().apply { value = null }
    val userNameAttachedToUserLiveData: LiveData<Boolean?>
        get() = _userNameAttachedToUserLiveData

    fun updateFirebaseUserName(userName: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                updateFirebaseUserNameScoped(userName)
            }
        }
    }

    private fun updateFirebaseUserNameScoped(userName: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()

        val task = user.updateProfile(profileUpdates)
        try {
            val result = Tasks.await(task)
            _userNameAttachedToUserLiveData.postValue(true)
            MAIN {
                Handler().postDelayed(
                    {
                        _userNameAttachedToUserLiveData.postValue(null)
                    },
                    500
                )
            }
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

    companion object {
        fun userNameRegExCheck(userName: String): Boolean {
            val pattern: Pattern =
                Pattern.compile("^(?=.{3,30}\$)(?![_.0-9])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])\$")
            val matcher: Matcher = pattern.matcher(userName)
            while (matcher.find()) {
//                println(userName.substring(matcher.start(), matcher.end()))
                return true
            }
            return false
        }
    }
}