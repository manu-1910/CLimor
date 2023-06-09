package com.limor.app.scenes.auth_new

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.firebase.FacebookAuthHandler
import com.limor.app.scenes.auth_new.navigation.AuthNavigator
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.PlayerViewManager
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_auth_new.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AuthActivityNew : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: AuthViewModelNew by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_auth_new)
        clActivityAuthNew.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        checkNavigationBreakPoint()
        Timber.d("Sign in case  ${model.signInCase}")
    }

    private fun checkNavigationBreakPoint() {
        lifecycleScope.launch {
            val activity = this@AuthActivityNew
            val breakpoint = PrefsHandler.loadNavigationBreakPoint(activity)
                ?: return@launch
            AuthNavigator.navigateToFragmentByNavigationBreakpoints(activity, breakpoint)
        }
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
        checkFirebaseEmailLogin(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkFirebaseEmailLogin(intent)
    }

    private fun checkFirebaseEmailLogin(sourceIntent: Intent?) {
        if (sourceIntent == null) {
            return
        }

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(sourceIntent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Timber.d("DeepLink fetched $deepLink")
                    model.handleEmailDynamicLink(this, deepLink.toString())
                }
            }
            .addOnFailureListener(this) { e -> Timber.e(e) }
    }

    companion object {
        const val GOOGLE_SIGN_REQUEST_CODE = 10001
        private const val TERMS_URL = "https://www.limor.ie/terms-and-conditions";
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