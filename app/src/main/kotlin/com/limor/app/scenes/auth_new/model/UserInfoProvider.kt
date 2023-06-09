package com.limor.app.scenes.auth_new.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.apollo.UserRepository
import com.limor.app.apollo.interceptors.AuthInterceptor
import com.limor.app.scenes.auth_new.data.DobInfo.Companion.parseForUserCreation
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.uimodels.UserExistsModel
import com.limor.app.uimodels.mapToUIModel
import com.onesignal.OneSignal
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

    private val _userExistsLiveData = MutableLiveData<UserExistsModel?>().apply { value = null }
    val userExists: LiveData<UserExistsModel?>
        get() = _userExistsLiveData

    private val _otpSent = MutableLiveData<String?>().apply { value = null }
    val otpSent: LiveData<String?>
        get() = _otpSent

    private val _otpValidWithToken = MutableLiveData<String?>().apply { value = null }
    val otpValid: LiveData<String?>
        get() = _otpValidWithToken

    private val _otpInValid = MutableLiveData<String?>().apply { value = null }
    val otpInValid: LiveData<String?>
        get() = _otpInValid

    private val _updateUserFirstNameAndLastNameLiveData =
        MutableLiveData<String?>().apply { value = null }
    val updateUserFirstNameAndLastNameLiveData: LiveData<String?>
        get() = _updateUserFirstNameAndLastNameLiveData

    private val _otpSentForDeleteAccount = MutableLiveData<String?>().apply { value = null }
    val otpSentDeleteAccount: LiveData<String?>
        get() = _otpSentForDeleteAccount

    private val _otpValidToDeleteUser = MutableLiveData<String?>().apply { value = null }
    val otpValidToDeleteUser: LiveData<String?>
        get() = _otpValidToDeleteUser

    @ExperimentalCoroutinesApi
    fun getUserOnboardingStatus(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            try {
                createDeviceToken().collect {
                    userRepository.createUserDevice(it)
                    FirebaseAuth.getInstance().uid?.let {
                        it1 -> OneSignal.setExternalUserId(it1)
                    }
                    OneSignal.getDeviceState()?.let { deviceState ->
                        if(deviceState.areNotificationsEnabled()){
                            userRepository.saveOneSignalId(deviceState.userId)
                        }
                    }
                }
                val response = userRepository.getUserOnboardingStatus() ?: ""
                val breakpoint = getBreakpointAccordingToEmailPresence(response)
                _breakPointLiveData.postValue(breakpoint)
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
        return response

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
                if (response == "Success") {
                    createDeviceToken().collect {
                        userRepository.createUserDevice(it)
                        FirebaseAuth.getInstance().uid?.let {
                            it1 -> OneSignal.setExternalUserId(it1)
                        }
                        OneSignal.getDeviceState()?.let { deviceState ->
                            if(deviceState.areNotificationsEnabled()){
                                userRepository.saveOneSignalId(deviceState.userId)
                            }
                        }
                    }
                }

                _createUserLiveData.postValue(response)
                getUserOnboardingStatus(this)
                delay(500)
                _createUserLiveData.postValue(null)
            } catch (e: Exception) {
                _userInfoProviderErrorLiveData.postValue(e.message)
                delay(500)
                _userInfoProviderErrorLiveData.postValue(null)
            }

        }
    }

    fun checkIfUserExistsWithThisPhoneNumber(scope: CoroutineScope, phoneNumber: String) {
        scope.launch {
            if (phoneNumber.isEmpty()) {
                _userExistsLiveData.postValue(UserExistsModel(false, isDeleted = false))
                delay(500)
                _userExistsLiveData.postValue(null)
            }
            try {
                val response = userRepository.getUserByPhoneNumber(phoneNumber)
                if (BuildConfig.DEBUG) {
                    println("Checking if $phoneNumber exists -> $response")
                }
                _userExistsLiveData.postValue(response?.mapToUIModel() ?: UserExistsModel(false, isDeleted = false))
                delay(500)
                _userExistsLiveData.postValue(null)
            } catch (exception: Exception) {
                exception.printStackTrace()
                _userExistsLiveData.postValue(UserExistsModel(false, isDeleted = false))
                delay(500)
                _userExistsLiveData.postValue(null)
            }

        }
    }

    fun sendOtpToPhoneNumber(scope: CoroutineScope, phoneNumber: String, isSignInCase: Boolean, resend: Boolean = false, reactivate: Boolean = false){
        scope.launch {
            if (phoneNumber.isEmpty()) {
                _otpSent.postValue("Invalid phone number")
                delay(500)
                _otpSent.postValue(null)
            }
            try {
                val response = if(isSignInCase) userRepository.sendOtpToPhoneNumber(phoneNumber, reactivate) else userRepository.sendOtpForSignUp(phoneNumber)
                if (BuildConfig.DEBUG) {
                    println("Sending otp to $phoneNumber -> $response")
                }
                _otpSent.postValue(response ?: "")
                delay(500)
                _otpSent.postValue(null)
            } catch (exception: Exception) {
                exception.printStackTrace()
                _otpSent.postValue(exception.localizedMessage)
                delay(500)
                _otpSent.postValue(null)
            }
        }
    }

    fun validateOtp(scope: CoroutineScope, phoneNumber: String, otp: Int, reActivate: Boolean = false){
        scope.launch(Dispatchers.Default) {
            try{
                val response = userRepository.validateUserOtp(phoneNumber, otp, reActivate)
                if(BuildConfig.DEBUG){
                    println("Validating otp for sign in case $response")
                }
                _otpValidWithToken.postValue(response)
                delay(500)
                _otpValidWithToken.postValue(null)
            } catch (e: Exception){
                e.printStackTrace()
                _otpInValid.postValue(e.localizedMessage)
                delay(500)
                _otpInValid.postValue(null)
            }
        }
    }

    fun validateOtpForSignUp(scope: CoroutineScope, phoneNumber: String, otp: Int, dob: Long){
        scope.launch(Dispatchers.Default) {
            try{
                val response = userRepository.validateUserOtpForSignUp(phoneNumber, otp, parseForUserCreation(dob))
                if(BuildConfig.DEBUG){
                    print("Validating otp for sign up $response")
                }
                _otpValidWithToken.postValue(response)
                delay(500)
                _otpValidWithToken.postValue(null)
            } catch (e: Exception){
                e.printStackTrace()
                _otpInValid.postValue(e.localizedMessage)
                delay(500)
                _otpInValid.postValue(null)
            }
        }
    }

    fun sendOtpToPhoneNumberToDeleteUserAccount(scope: CoroutineScope, phoneNumber: String, resend: Boolean = false){
        scope.launch {
            if (phoneNumber.isEmpty()) {
                _otpSentForDeleteAccount.postValue("The given phone number is invalid.")
                delay(500)
                _otpSentForDeleteAccount.postValue(null)
            }
            try {
                val response = userRepository.sendOtpToDeleteAccount(phoneNumber)
                if (BuildConfig.DEBUG) {
                    println("Sending otp to $phoneNumber -> $response")
                }
                _otpSentForDeleteAccount.postValue(response ?: "")
                delay(500)
                _otpSentForDeleteAccount.postValue(null)
            } catch (exception: Exception) {
                exception.printStackTrace()
                _otpSentForDeleteAccount.postValue(exception.localizedMessage)
                delay(500)
                _otpSentForDeleteAccount.postValue(null)
            }
        }
    }

    fun validateOtpToDeleteUserAccount(scope: CoroutineScope, phoneNumber: String, otp: Int){
        scope.launch(Dispatchers.Default) {
            try{
                val response = userRepository.validateOtpToDeleteUser(phoneNumber, otp)
                if(BuildConfig.DEBUG){
                    println("Validating otp for sign in case $response")
                }
                _otpValidToDeleteUser.postValue(response)
                delay(500)
                _otpValidToDeleteUser.postValue(null)
            } catch (e: Exception){
                e.printStackTrace()
                _otpInValid.postValue(e.localizedMessage)
                delay(500)
                _otpInValid.postValue(null)
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun createDeviceToken() = callbackFlow<String> {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
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

    fun updateUserFirstNameAndLastName(scope: CoroutineScope, firstName: String, lastName: String) {
        scope.launch(Dispatchers.Default) {
            try {
                val response = userRepository.updateFirstNameAndLastName(firstName, lastName)
                _updateUserFirstNameAndLastNameLiveData.postValue(response)
                delay(500)
                _updateUserFirstNameAndLastNameLiveData.postValue(null)
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun updateGender(
        scope: CoroutineScope,
        gender: Int
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                val result = userRepository.updateUserGender(gender)
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

    fun updateLanguagesAndCategories(
        scope: CoroutineScope,
        categories: List<Int?>,
        languages: List<String?>
    ) {
        scope.launch(Dispatchers.Default) {
            try {
                val result = userRepository.updateLanguagesAndCategories(categories, languages)
                _updatePreferredInfoLiveData.postValue(result)
                delay(500)
                _updatePreferredInfoLiveData.postValue(null)
            } catch (e: java.lang.Exception) {
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
        genderId: Int,
        userName: String,
        firstName: String,
        lastName: String,
        bio: String,
        website: String,
        imageURL: String?,
        voiceBioURL: String?,
        durationSeconds: Double?
    ): String? {
        return userRepository.updateUserProfile(
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

    suspend fun reportUser(id: Int, reason: String) {
        try {
            userRepository.reportUser(id, reason)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        try {
            userRepository.updateUserNotificationStatus(enabled);
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun deleteUserDevice(){
        try{
            val result = userRepository.deleteUserDevice()
        } catch (e: Exception){
            Timber.e(e)
            null
        }
    }

}