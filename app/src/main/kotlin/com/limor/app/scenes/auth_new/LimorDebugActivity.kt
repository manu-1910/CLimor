package com.limor.app.scenes.auth_new

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.scenes.auth_new.firebase.FirebaseSessionHandler
import kotlinx.android.synthetic.main.activity_limor_debug.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LimorDebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limor_debug)
        clMain.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setOnClicks()
    }

    private fun setOnClicks() {
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                FirebaseSessionHandler.logout(applicationContext)
                delay(300)
                Toast.makeText(this@LimorDebugActivity, "Done!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LimorDebugActivity,
                    "Error -> ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}