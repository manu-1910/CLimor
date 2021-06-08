package com.limor.app.scenes.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.SessionManager
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.main.MainActivity
import javax.inject.Inject

class SplashActivity : BaseActivity() {
    private var mDelayHandler: Handler? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            val hasFirebaseUser = true // FirebaseAuth.getInstance().currentUser != null
            if (hasFirebaseUser) {
                //println("client_id es:" + sessionManager.getStoredUser().id)
                var mainIntent: Intent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                this.finish()
            } else {
                startActivity(Intent(applicationContext, AuthActivityNew::class.java))
                finish()
            }
        }
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

    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

}