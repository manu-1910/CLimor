package com.limor.app.scenes.auth_new.firebase

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler.reAuthWithPhoneCredential
import com.limor.app.scenes.utils.MAIN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


object EmailAuthHandler {

    private val _emailIsInUseLiveData = MutableLiveData<Boolean?>().apply { value = null }
    val emailIsInUseLiveData: LiveData<Boolean?>
        get() = _emailIsInUseLiveData

    private val _emailAttachedToUserLiveData = MutableLiveData<Boolean?>().apply { value = null }
    val emailAttachedToUserLiveData: LiveData<Boolean?>
        get() = _emailAttachedToUserLiveData

    private val _emailAuthErrorLiveData = MutableLiveData<String?>().apply { value = null }
    val emailAuthErrorLiveData: LiveData<String?>
        get() = _emailAuthErrorLiveData

    fun checkEmailIsInUse(email: String, scope: CoroutineScope) {
        scope.launch {
            withContext(Dispatchers.IO) {
                checkEmailIsInUseScoped(email)
            }
        }
    }

    private fun checkEmailIsInUseScoped(email: String) {
        val task = FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
        val resultSignInMethod = try {
            val result = Tasks.await(task)
            val signInMethods = result.signInMethods
            when {
                signInMethods?.size ?: 0 != 0 -> true
//                signInMethods!!.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) -> {
//                    // User can sign in with email/password
//                    Timber.d("checkEmailIsInUse -> User can sign in with email/password")
//                    true
//                }
//                signInMethods.contains(EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD) -> {
//                    // User can sign in with email/link
//                    Timber.d("checkEmailIsInUse -> User can sign in with email/link")
//                    true
//                }
                else -> {
                    Timber.d("checkEmailIsInUse -> User does not contain email")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting sign in methods for user")
            false
        }
        Timber.d("Posting _emailIsInUseLiveData value $resultSignInMethod")
        _emailIsInUseLiveData.postValue(resultSignInMethod)
        MAIN { Handler().postDelayed({ _emailIsInUseLiveData.postValue(null) }, 500) }

    }

    fun addEmailToUser(email: String, scope: CoroutineScope) {
        scope.launch {
            withContext(Dispatchers.IO) {
                addEmailToFirebaseAccount(email)
            }
        }
    }

    private fun addEmailToFirebaseAccount(email: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val task = user.updateEmail(email)
        try {
            val result = Tasks.await(task)
            _emailAttachedToUserLiveData.postValue(true)
            MAIN {
                Handler().postDelayed({ _emailAttachedToUserLiveData.postValue(null) }, 500)
            }
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Timber.e(e)
            try {
                reAuthWithPhoneCredential()
            } catch (e: Exception) {
                Timber.e(e)
                _emailAuthErrorLiveData.postValue(e.cause?.message ?: e.message)
            }
        } catch (e: Exception) {
            Timber.e(e)
            _emailAuthErrorLiveData.postValue(e.cause?.message ?: e.message)
        }
    }
}