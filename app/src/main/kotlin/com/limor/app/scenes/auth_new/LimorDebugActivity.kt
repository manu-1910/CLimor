package com.limor.app.scenes.auth_new

import android.Manifest.permission.RECORD_AUDIO
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.limor.app.R
import com.limor.app.util.checkPermission

class LimorDebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limor_debug)
//        clMain.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        setOnClicks()

        checkPermission(RECORD_AUDIO, 123)
    }

//    private fun setOnClicks() {
//        btnLogout.setOnClickListener {
//            logout()
//        }
//    }
//
//    private fun logout() {
//        lifecycleScope.launch {
//            try {
//                FirebaseSessionHandler.logout(applicationContext)
//                delay(300)
//                Toast.makeText(this@LimorDebugActivity, "Done!", Toast.LENGTH_LONG).show()
//            } catch (e: Exception) {
//                Toast.makeText(
//                    this@LimorDebugActivity,
//                    "Error -> ${e.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }
}