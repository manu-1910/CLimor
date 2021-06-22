package com.limor.app.scenes.auth_new.firebase

import android.app.Activity
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface ContextProviderHandler {
    fun activity(): Activity
}

object PhoneAuthHandler : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var storedVerificationId: String? = null
    private var phoneAuthCredential: PhoneAuthCredential? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var scope: CoroutineScope
    private lateinit var contextProviderHandler: ContextProviderHandler

    private val _smsCodeValidationErrorMessage =
        MutableLiveData<String>().apply { value = "" }
    val smsCodeValidationErrorMessage: LiveData<String>
        get() = _smsCodeValidationErrorMessage

    private val _smsCodeValidationPassed =
        MutableLiveData<Boolean>().apply { value = false }
    val smsCodeValidationPassed: LiveData<Boolean>
        get() = _smsCodeValidationPassed

    fun init(scope: CoroutineScope, contextProviderHandler: ContextProviderHandler) {
        this.scope = scope
        this.contextProviderHandler = contextProviderHandler
    }

    fun sendCodeToPhone(phone: String, resend: Boolean = false) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(contextProviderHandler.activity())
            .setCallbacks(this)
        if (resend && resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        Timber.d("onVerificationCompleted:$credential")
        Toast.makeText(contextProviderHandler.activity(), "Auto verification", Toast.LENGTH_LONG)
            .show()
        signInWithPhoneAuthCredential(credential)
    }

    override fun onVerificationFailed(e: FirebaseException) {
        Timber.e(e, "onVerificationFailed")
        if (e is FirebaseAuthInvalidCredentialsException) {
            // Invalid request
        } else if (e is FirebaseTooManyRequestsException) {
            // The SMS quota for the project has been exceeded
        }
        _smsCodeValidationErrorMessage.postValue(e.localizedMessage)

    }

    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        // The SMS verification code has been sent to the provided phone number, we
        // now need to ask the user to enter the code and then construct a credential
        // by combining the code with a verification ID.
        Timber.d("onCodeSent: $verificationId")
        Toast.makeText(contextProviderHandler.activity(), "Code has been sent", Toast.LENGTH_LONG)
            .show()
        // Save verification ID and resending token so we can use them later
        storedVerificationId = verificationId
        resendToken = token
    }

    override fun onCodeAutoRetrievalTimeOut(message: String) {
        Timber.d("onCodeAutoRetrievalTimeOut: $message")
    }

    fun enterCodeAndSignIn(code: String) {
        scope.launch {
            _smsCodeValidationErrorMessage.postValue("")
            if (storedVerificationId == null) return@launch
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    fun reAuthWithPhoneCredential(): AuthCredential? {
        if (phoneAuthCredential != null) {
            val task = Tasks.await(auth.signInWithCredential(phoneAuthCredential!!))
            return task.credential
        }
        return null
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        phoneAuthCredential = credential
        auth.signInWithCredential(credential)
            .addOnCompleteListener(contextProviderHandler.activity()) { task ->
                val successful = task.isSuccessful
                if (successful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")

                    val user = task.result?.user
                    Timber.d("signed user phone: ${user?.phoneNumber}")
                } else {
                    // Sign in failed, display a message and update the UI
                    Timber.e(task.exception, "signInWithCredential:failure")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    _smsCodeValidationErrorMessage.postValue(task.exception?.localizedMessage ?: "")
                }
                if (successful) {
                    _smsCodeValidationPassed.postValue(true)
                    Handler().postDelayed({ _smsCodeValidationPassed.postValue(false) }, 500)
                }
            }
    }

    fun clearError() {
        _smsCodeValidationErrorMessage.postValue("")
    }
}