package com.limor.app.scenes.auth_new.firebase

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.limor.app.App
import com.limor.app.R
import com.limor.app.scenes.auth_new.model.SmsCodeValidationResponse
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PhoneAuthHandler @Inject constructor() :
    PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var phoneAuthCredential: PhoneAuthCredential? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var scope: CoroutineScope
    private var shouldSendCode = true
    private var isSignInCase = false

    private val _smsCodeValidationErrorMessage =
        MutableLiveData<SmsCodeValidationResponse?>().apply { value = null }
    val smsCodeValidationErrorMessage: LiveData<SmsCodeValidationResponse?>
        get() = _smsCodeValidationErrorMessage

    private val _smsCodeValidationPassed =
        MutableLiveData<Boolean>().apply { value = false }
    val smsCodeValidationPassed: LiveData<Boolean>
        get() = _smsCodeValidationPassed

    private val _codeSentListener =
        MutableLiveData<Boolean>().apply { value = false }
    val codeSentListener: LiveData<Boolean>
        get() = _codeSentListener

    private val _authCustomTokenResult =
        MutableLiveData<Boolean?>().apply { value = null }
    val authCustomTokenResult: LiveData<Boolean?>
        get() = _authCustomTokenResult

    fun init(activityReference: WeakReference<Activity>, scope: CoroutineScope) {
        activityRef = activityReference
        this.scope = scope
    }

    fun sendCodeToPhone(phone: String, resend: Boolean = false, isSignInCase: Boolean) {
        this.isSignInCase = isSignInCase
        if (activity == null) return
        if (resend)
            shouldSendCode = true

        if (!(shouldSendCode)) return

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(120, TimeUnit.SECONDS)
            .setActivity(activity!!)
            .setCallbacks(this)
        if (resend && resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        shouldSendCode = false
    }

    private fun linkPhone(credential: PhoneAuthCredential, onDone: () -> Unit = {}) {
        val firebaseUser = Firebase.auth.currentUser

        if (firebaseUser == null) {
            Timber.d("No Firebase user to link a phone to.")
            onDone()
            return
        }

        val task = firebaseUser.linkWithCredential(credential)
        task.addOnCompleteListener {
            println("onVerificationCompleted: Linked user's phone? ${it.isSuccessful} -> ${it.exception}")
            onDone()
        }
        task.addOnSuccessListener {
            println("onVerificationCompleted: Linked user's phone successfully -> ${ it.additionalUserInfo }")
            onDone()
        }
        task.addOnFailureListener {
            println("onVerificationCompleted: Could not link user's phone. Failed with $it")
            onDone()
        }
    }

    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        onVerificationCompleted(credential, true)
    }

    private fun onVerificationCompleted(credential: PhoneAuthCredential, isAuto: Boolean) {
        Timber.d("onVerificationCompleted:$credential")

        if (isAuto) {
            activity?.let { Toast.makeText(it, "Auto verification", Toast.LENGTH_LONG).show() }
        }

        Firebase.auth.currentUser?.let {
            if (it.isEmailVerified && it.phoneNumber.isNullOrEmpty()) {
                // user already has email, this is a migration user, so we link his phone number with
                // the account and consider his phone auth successful
                linkPhone(credential) {
                    onPhoneAuthSuccess()
                }
                return
            }
        }

        signInWithPhoneAuthCredential(credential)
    }

    override fun onVerificationFailed(e: FirebaseException) {
        Timber.e(e, "onVerificationFailed")
        if (e is FirebaseAuthInvalidCredentialsException) {
            // Invalid request
        } else if (e is FirebaseTooManyRequestsException) {
            // The SMS quota for the project has been exceeded
        }
        _smsCodeValidationErrorMessage.postValue(SmsCodeValidationResponse(e))

    }

    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        // The SMS verification code has been sent to the provided phone number, we
        // now need to ask the user to enter the code and then construct a credential
        // by combining the code with a verification ID.
        Timber.d("onCodeSent: $verificationId")
        Toast.makeText(activity, "Code has been sent", Toast.LENGTH_LONG)
            .show()

        // Save verification ID and resending token so we can use them later
        PrefsHandler.setLastVerificationId(App.instance, verificationId)

        resendToken = token
        _codeSentListener.value = true
    }

    override fun onCodeAutoRetrievalTimeOut(message: String) {
        Timber.d("onCodeAutoRetrievalTimeOut: $message")
    }

    fun enterCodeAndSignIn(code: String) {
        if (code.isEmpty()) {
            activity?.let {
                _smsCodeValidationErrorMessage.postValue(SmsCodeValidationResponse(SmsCodeValidationResponse.ErrorType.NO_CODE_ENTERED))
            }
            return
        }
        scope.launch {
            // this would enable the 'Continue' button in the FragmentVerifyPhoneNumber, which we
            // don't need as it's confusing you can continue while the phone verification is
            // ongoing and can make users click on the button leading to a secondary verification
            // process
            // _smsCodeValidationErrorMessage.postValue("")

            val storedVerificationId = PrefsHandler.getLastVerificationId(App.instance)
            if (storedVerificationId.isNullOrEmpty()) {
                // we should always send an error message regardless of acitivty's availability
                // otherwise sending an empty message means "no error"
                val message = activity?.getString(R.string.code_hasnt_been_sent) ?: "Code has not been sent."
                _smsCodeValidationErrorMessage.postValue(SmsCodeValidationResponse(SmsCodeValidationResponse.ErrorType.CODE_HAS_NOT_BEEN_SENT))
                return@launch
            }

            val credential: PhoneAuthCredential?
            try {
                credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            } catch (t: Throwable) {
                t.printStackTrace()

                val message = activity?.getString(R.string.error_getting_credentials)
                        ?: "Error creating credential. Please try again."
                _smsCodeValidationErrorMessage.postValue(SmsCodeValidationResponse(SmsCodeValidationResponse.ErrorType.CREDENTIAL_ERROR))

                return@launch
            }

            onVerificationCompleted(credential, false)
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
        if (activity == null) return
        shouldSendCode = true
        phoneAuthCredential = credential
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
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

                    // We should always notify about an error even if an exception isn't available
                    val message = task.exception?.localizedMessage ?: "Could not sign in. Generic error."
                    _smsCodeValidationErrorMessage.postValue(if(task.exception != null) SmsCodeValidationResponse(task.exception) else SmsCodeValidationResponse(SmsCodeValidationResponse.ErrorType.GENERIC_ERROR))
                }
                if (successful) {
                    onPhoneAuthSuccess()
                }
            }
    }

    fun signInWithCustomToken(token: String){
        auth.signInWithCustomToken(token).addOnCompleteListener(activity!!){ task ->
            val isSuccessfull = task.isSuccessful
            if(isSuccessfull){
                _smsCodeValidationPassed.postValue(true)
            } else{
                _smsCodeValidationPassed.postValue(false)
            }
        }
    }

    private fun onPhoneAuthSuccess() {
        scope.launch {
            if (!isSignInCase) {
                onPhoneAuthSuccessPositive()
                return@launch
            }
            if (JwtChecker.isFirebaseJwtContainsLuid())
                onPhoneAuthSuccessPositive()
            else
                onPhoneAuthSuccessNegative()
        }
    }

    private suspend fun onPhoneAuthSuccessPositive() {
        _smsCodeValidationPassed.postValue(true)
        delay(300)
        _smsCodeValidationPassed.postValue(false)
    }

    private fun onPhoneAuthSuccessNegative() {
        //This is possible if user Signing in, but did not create Limor account before (doesn't have "luid" in JWT)
        val message = activity?.getString(R.string.no_user_found_offer_to_sign_up) ?: ""
        _smsCodeValidationErrorMessage.postValue(SmsCodeValidationResponse(SmsCodeValidationResponse.ErrorType.USER_NOT_FOUND))
    }

    fun clearError() {
        _smsCodeValidationErrorMessage.postValue(null)
    }

    private lateinit var activityRef: WeakReference<Activity>
    private val activity
        get() = try {
            activityRef.get()!!
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
}
