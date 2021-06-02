package com.limor.app.scenes.auth_new.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PrefsHandler {

    private  const val LABEL_AUTH_NEW_PREFS = "auth_new_prefs"
    private  const val LABEL_AUTH_EMAIL_SIGN_IN = "auth_email_sign_in"

    fun saveEmailToSignIn(context: Context, email: String){
        val mPrefs: SharedPreferences = sharedPreferences(context)
        mPrefs.edit(true) {
            putString(LABEL_AUTH_EMAIL_SIGN_IN, email)
        }
    }

    fun loadEmailToSignIn(context: Context): String?{
        val mPrefs: SharedPreferences = sharedPreferences(context)
        return mPrefs.getString(LABEL_AUTH_EMAIL_SIGN_IN, null)
    }

    private fun sharedPreferences(context: Context) =
        context.getSharedPreferences(LABEL_AUTH_NEW_PREFS, Context.MODE_PRIVATE)
}