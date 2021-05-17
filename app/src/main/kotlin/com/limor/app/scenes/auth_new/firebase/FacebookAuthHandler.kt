package com.limor.app.scenes.auth_new.firebase

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

object FacebookAuthHandler : FacebookCallback<LoginResult> {

    private val auth = FirebaseAuth.getInstance()
    private var facebookCredentials: AuthCredential? = null
    val callbackManager: CallbackManager = CallbackManager.Factory.create()

    private val _facebookLoginSuccess = MutableLiveData<Boolean>().apply { value = false }
    val facebookLoginSuccess: LiveData<Boolean>
        get() = _facebookLoginSuccess

    private val _facebookLoginError = MutableLiveData<String?>().apply { value = null }
    val facebookLoginError: LiveData<String?>
        get() = _facebookLoginError

    fun clearError() {
        _facebookLoginError.postValue(null)
    }

    override fun onSuccess(result: LoginResult?) {
        if (result == null) return
        Timber.d("Facebook login result: success")
        handleFacebookAccessToken(result.accessToken)
    }

    override fun onCancel() {
        Timber.d("Facebook login result: cancel")
    }

    override fun onError(error: FacebookException?) {
        Timber.e(error, "Facebook login result: cancel")
        _facebookLoginError.postValue(error?.message)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Timber.d("handleFacebookAccessToken:$token")
        facebookCredentials = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(facebookCredentials!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Timber.d("signInWithCredential:success ${user?.email}")
                    _facebookLoginSuccess.postValue(true)
                    Handler().postDelayed({ _facebookLoginSuccess.postValue(false) }, 500)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e(task.exception, "signInWithCredential:failure")
                    _facebookLoginError.postValue(task.exception?.message)
                }
            }
    }
}