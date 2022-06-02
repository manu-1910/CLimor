package com.limor.app.scenes.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.SessionManager
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.util.AppState
import com.onesignal.OSNotificationAction
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal.OSNotificationOpenedHandler


class SplashActivity : BaseActivity() {
    private var mDelayHandler: Handler? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val mRunnable: Runnable = Runnable {
        if (isFinishing) return@Runnable

        lifecycleScope.launch {
            val navigationFlowIsFinished = navigationFlowIsFinished()
            val hasFirebaseUser = FirebaseAuth.getInstance().currentUser != null
            if (hasFirebaseUser && navigationFlowIsFinished) {
                val activity = this@SplashActivity
                val mainIntent = Intent(activity, MainActivityNew::class.java)
                startActivity(mainIntent)
                activity.finish()
            } else {
                startActivity(Intent(applicationContext, AuthActivityNew::class.java))
                finish()
            }
        }
    }

    private fun navigationFlowIsFinished(): Boolean {
        val breakpoint = PrefsHandler.loadNavigationBreakPoint(this)
            ?: return false
        return breakpoint == NavigationBreakpoints.HOME_FEED.destination
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(
            mRunnable,
            resources.getInteger(R.integer.SPLASH_DELAY).toLong()
        )
    }

    private fun isEmailSignIn(intent: Intent): Boolean {
        val auth = Firebase.auth
        val emailLink = intent.data.toString()

        return auth.isSignInWithEmailLink(emailLink)
    }

    override fun onStart() {
        super.onStart()
        PrefsHandler.setPreferencesScreenOpenedInThisSession(this, false)
        if (isEmailSignIn(intent)) {
            // Forward this to the AuthActivity
            val authIntent = Intent(applicationContext, AuthActivityNew::class.java)
            authIntent.data = intent.data
            authIntent.putExtras(intent)
            startActivity(authIntent)
            finish()
            return
        }

        OneSignal.setNotificationOpenedHandler { result ->
            val id: Int? = result.notification.additionalData.getString("targetId").toInt()
            var tabId = 0
            if (result.notification.additionalData.has("notificationType") && result.notification.additionalData.getString("notificationType") == "patronRequest") {
                tabId = 1
            }
            if(result.notification.additionalData.getString("targetType").equals("user")){
                id?.let {
                    PrefsHandler.saveUserIdFromOneSignalNotification(this, it)
                    PrefsHandler.saveUserNameFromOneSignalNotification(this, result.notification.additionalData.getString("initiatorUsername"))
                    PrefsHandler.saveUserTabIdFromOneSignalNotification(this, tabId)
                }
            } else{
                id?.let {
                    PrefsHandler.savePodCastIdOfSharedLink(this, it)
                }
            }
        }

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val td: Int? = deepLink?.getQueryParameter("id")?.toInt()
                    td?.let {
                        PrefsHandler.savePodCastIdOfSharedLink(this, it)
                    }

                    Timber.d("DeepLink fetched $deepLink")
                }
            }
            .addOnFailureListener(this) { e -> Timber.e(e) }
    }

    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

}