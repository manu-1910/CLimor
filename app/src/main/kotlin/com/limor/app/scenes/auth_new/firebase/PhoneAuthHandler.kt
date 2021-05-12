package com.limor.app.scenes.auth_new.firebase

import android.app.Activity
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.limor.app.scenes.utils.BACKGROUND
import timber.log.Timber
import java.util.concurrent.TimeUnit

object PhoneAuthHandler : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var storedVerificationId: String? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var activity: Activity
    val smsCodeValidatedLiveData = MutableLiveData<Boolean>().apply { value = false }
    val smsCodeValidationErrorMessage =
        MutableLiveData<String>().apply { value = "" }

    fun init(activity: Activity) {
        this.activity = activity
    }

    fun sendCodeToPhone(phone: String, resend: Boolean = false) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(this)
        if (resend && resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        Timber.d("onVerificationCompleted:$credential")
        signInWithPhoneAuthCredential(credential)
    }

    override fun onVerificationFailed(e: FirebaseException) {
        Timber.e(e, "onVerificationFailed")
        if (e is FirebaseAuthInvalidCredentialsException) {
            // Invalid request
        } else if (e is FirebaseTooManyRequestsException) {
            // The SMS quota for the project has been exceeded
        }
        smsCodeValidationErrorMessage.postValue(e.toString())

        // Show a message and update the UI
    }

    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        // The SMS verification code has been sent to the provided phone number, we
        // now need to ask the user to enter the code and then construct a credential
        // by combining the code with a verification ID.
        Timber.d("onCodeSent: $verificationId")

        // Save verification ID and resending token so we can use them later
        storedVerificationId = verificationId
        resendToken = token
    }

    override fun onCodeAutoRetrievalTimeOut(message: String) {
        Timber.d("onCodeAutoRetrievalTimeOut: $message")
    }

    fun enterCodeAndSignIn(code: String) {
        BACKGROUND({
            smsCodeValidationErrorMessage.postValue("")
            if (storedVerificationId == null) return@BACKGROUND
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        })
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
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
                    smsCodeValidationErrorMessage.postValue(task.exception.toString())
                }
                smsCodeValidatedLiveData.postValue(successful)
                if (successful)
                    Handler().postDelayed({ smsCodeValidatedLiveData.postValue(false) }, 500)
            }
    }

    fun clearError() {
        smsCodeValidationErrorMessage.postValue("")
    }
}