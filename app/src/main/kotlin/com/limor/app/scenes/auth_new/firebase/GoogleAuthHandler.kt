package com.limor.app.scenes.auth_new.firebase

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew.Companion.GOOGLE_SIGN_REQUEST_CODE
import com.limor.app.scenes.utils.BACKGROUND
import com.limor.app.scenes.utils.MAIN
import timber.log.Timber


object GoogleAuthHandler {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInAccount: GoogleSignInAccount
    private lateinit var googleAuthCredential: AuthCredential

    private val _googleSignIsComplete = MutableLiveData<Boolean>().apply { value = false }
    val googleSignIsComplete: LiveData<Boolean>
        get() = _googleSignIsComplete

    private val _googleLoginError = MutableLiveData<String?>().apply { value = null }
    val googleLoginError: LiveData<String?>
        get() = _googleLoginError

    fun clearError(){
        _googleLoginError.postValue(null)
    }

    fun initClientAndSignIn(activity: Activity) {
        BACKGROUND({
            val googleServicesAreAvailable = checkPlayServices(activity)
            if (!googleServicesAreAvailable) return@BACKGROUND
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // commenting this out because default_web_client_id doesn't exist
                // .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestId()
                .build()

            googleSignInClient = GoogleSignIn.getClient(activity, gso)
            MAIN { signIn(activity) }
        })
    }

    private fun signIn(activity: Activity) {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, GOOGLE_SIGN_REQUEST_CODE)
    }

    fun handleResultIntent(data: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            googleSignInAccount = task.getResult(ApiException::class.java)!!

            Timber.d(
                "%s%s%s%s",
                "firebaseAuthWithGoogle: ",
                googleSignInAccount.id,
                " ",
                googleSignInAccount.email
            )
            firebaseAuthWithGoogle(googleSignInAccount.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Timber.e(e, "Google sign in failed")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        googleAuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(googleAuthCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val userEmail = FirebaseAuth.getInstance().currentUser?.email
                    Timber.d("signInWithCredential : success $userEmail")
                    _googleSignIsComplete.postValue(true)
                    Handler().postDelayed({ _googleSignIsComplete.postValue(false) }, 500)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e(task.exception, "signInWithCredential:failure")
                    _googleSignIsComplete.postValue(false)
                }
            }
    }

    private fun checkPlayServices(activity: Activity): Boolean {
        val gApi = GoogleApiAvailability.getInstance()
        val resultCode = gApi.isGooglePlayServicesAvailable(activity)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (gApi.isUserResolvableError(resultCode)) {
                MAIN {
                    gApi.getErrorDialog(activity, resultCode, -10001)?.show()
                }
            } else {
                MAIN {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.google_services_not_available),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return false
        }
        return true
    }
}