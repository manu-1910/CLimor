package com.limor.app.scenes.auth_new

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.firebase.FacebookAuthHandler
import kotlinx.android.synthetic.main.activity_auth_new.*
import timber.log.Timber

class AuthActivityNew : AppCompatActivity() {

    private val model: AuthViewModelNew by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_new)
        clActivityAuthNew.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        FirebaseAuth.getInstance().firebaseAuthSettings
            .setAppVerificationDisabledForTesting(true)
    }

    fun launchTermsUrl() {
        val uri = Uri.parse(TERMS_URL)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null)
            return
        if (requestCode == GOOGLE_SIGN_REQUEST_CODE) {
            model.handleGoogleAuthResult(data)
            return
        }
        FacebookAuthHandler.callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Timber.d("DeepLink fetched $deepLink")
                    model.handleEmailDynamicLink(this,  deepLink.toString())
                }
            }
            .addOnFailureListener(this) { e -> Timber.e(e) }
    }

    companion object {
        const val GOOGLE_SIGN_REQUEST_CODE = 10001
        private const val TERMS_URL = "https://www.limor.ie/terms-and-conditions-of-use";
        fun onFocusChangeListener(): View.OnFocusChangeListener {
            return View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus)
                    v.hideKeyboard()
            }
        }

        fun popBackStack(activity: Activity) {
            try {
                val navController = Navigation.findNavController(activity, R.id.nav_host_fragment)
                val popResult = navController.popBackStack()
                if (!popResult) activity.finish()
            } catch (e: java.lang.Exception) {
                Timber.d("No backStack, finishing")
                activity.finish()
            }
        }
    }
}