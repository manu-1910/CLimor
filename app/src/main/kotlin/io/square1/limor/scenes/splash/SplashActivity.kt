package io.square1.limor.scenes.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.authentication.AuthenticationActivity
import javax.inject.Inject


class SplashActivity : BaseActivity() {
    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 2000 //2 seconds

    @Inject
    lateinit var sessionManager: SessionManager

    internal val mRunnable: Runnable = Runnable {
        if (!isFinishing) {

            if(!sessionManager.getStoredSession().isNullOrEmpty()){

              /*  var mainIntent: Intent
                mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                this.finish()*/

            }else{
                startActivity(Intent(applicationContext, AuthenticationActivity::class.java))
                finish()

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
    }

    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

}