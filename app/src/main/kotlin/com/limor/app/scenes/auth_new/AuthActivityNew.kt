package com.limor.app.scenes.auth_new

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.appcompat.app.AppCompatActivity
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import kotlinx.android.synthetic.main.activity_auth_new.*
import timber.log.Timber

class AuthActivityNew : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_new)
        clActivityAuthNew.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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

    companion object {
        private const val TERMS_URL = "https://www.limor.ie/terms-and-conditions-of-use";
        fun onFocusChangeListener(): View.OnFocusChangeListener {
            return View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus)
                    v.hideKeyboard()
            }
        }
    }
}