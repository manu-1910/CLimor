package com.limor.app.scenes.auth_new.firebase

import android.content.Context
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.limor.app.BuildConfig
import com.limor.app.scenes.auth_new.firebase.PhoneAuthHandler.reAuthWithPhoneCredential
import com.limor.app.scenes.auth_new.util.PrefsHandler
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

    private val _emailLinkSentLiveData = MutableLiveData<Boolean?>().apply { value = null }
    val emailLinkSentLiveData: LiveData<Boolean?>
        get() = _emailLinkSentLiveData

    private val _handleEmailDynamicLinkLiveData = MutableLiveData<Boolean?>().apply { value = null }
    val handleEmailDynamicLinkLiveData: LiveData<Boolean?>
        get() = _handleEmailDynamicLinkLiveData

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
                MAIN { Handler().postDelayed({ _emailAuthErrorLiveData.postValue(null) }, 500) }
            }
        } catch (e: Exception) {
            Timber.e(e)
            _emailAuthErrorLiveData.postValue(e.cause?.message ?: e.message)
            MAIN { Handler().postDelayed({ _emailAuthErrorLiveData.postValue(null) }, 500) }
        }
    }

    const val dynamicLink = "https://limorapp.page.link/open"

    fun sendFirebaseDynamicLinkToEmailScoped(context: Context, email: String, scope: CoroutineScope) {
        scope.launch {
            withContext(Dispatchers.IO) {
                sendFirebaseDynamicLinkToEmailScoped(context, email)
            }
        }
    }

    private fun sendFirebaseDynamicLinkToEmailScoped(context: Context, email: String) {

        val actionCodeSettings =
            ActionCodeSettings.newBuilder() // URL you want to redirect back to. The domain (www.example.com) for this
                // URL must be whitelisted in the Firebase Console.
                .setUrl("$dynamicLink?bar=foo") // This must be true
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    BuildConfig.APPLICATION_ID,
                    true,  /* installIfNotAvailable */
                    "41" /* minimumVersion */
                )
                .build()

        val auth = FirebaseAuth.getInstance()
        val task = auth.sendSignInLinkToEmail(email, actionCodeSettings)
        try {
            Tasks.await(task)
            PrefsHandler.saveEmailToSignIn(context, email)
            _emailLinkSentLiveData.postValue(true)
            MAIN { Handler().postDelayed({ _emailLinkSentLiveData.postValue(null) }, 500) }
        } catch (e: Exception) {
            Timber.e(e)
            _emailAuthErrorLiveData.postValue(e.cause?.message ?: e.message)
            MAIN { Handler().postDelayed({ _emailAuthErrorLiveData.postValue(null) }, 500) }
        }
    }

    fun handleDynamicLink(context: Context, emailLink: String, scope: CoroutineScope) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val email = PrefsHandler.loadEmailToSignIn(context)
                handleDynamicLinkScoped(emailLink, email?:"")
            }
        }
    }

    private fun handleDynamicLinkScoped(emailLink: String, email: String) {

        val auth = FirebaseAuth.getInstance()

        // Confirm the link is a sign-in with email link.
        Timber.d("handleDynamicLinkScoped")
        if (auth.isSignInWithEmailLink(emailLink)) {
            Timber.d("auth.isSignInWithEmailLink true")
            // Retrieve this from wherever you stored it
            // The client SDK will parse the code from the link for you.
            val task =
            auth.signInWithEmailLink(email, emailLink)
            try {
                val result = Tasks.await(task)

                _handleEmailDynamicLinkLiveData.postValue(true)
                MAIN {
                    Handler().postDelayed({ _handleEmailDynamicLinkLiveData.postValue(null) }, 500)
                }
            }catch (e:Exception){
                _emailAuthErrorLiveData.postValue(e.cause?.message ?: e.message)
                MAIN { Handler().postDelayed({ _emailAuthErrorLiveData.postValue(null) }, 500) }
            }
        }

    }
}