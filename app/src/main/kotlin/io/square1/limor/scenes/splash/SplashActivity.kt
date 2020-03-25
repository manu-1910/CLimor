package io.square1.limor.scenes.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.MainActivity
import javax.inject.Inject

class SplashActivity : BaseActivity() {
    private var mDelayHandler: Handler? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {

            if(!sessionManager.getStoredSession().isNullOrEmpty()){

              /*  var mainIntent: Intent
                mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                this.finish()*/

            }else{
                //startActivity(Intent(applicationContext, SignActivity::class.java))
                startActivity(Intent(applicationContext, MainActivity::class.java)) //for faster development //TODO
                finish()

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, resources.getInteger(R.integer.SPLASH_DELAY).toLong())
    }

    public override fun onDestroy() {
        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }

}