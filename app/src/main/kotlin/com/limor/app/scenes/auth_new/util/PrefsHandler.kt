package com.limor.app.scenes.auth_new.util

import android.content.Context
import androidx.core.content.edit

object PrefsHandler {

    private const val LABEL_AUTH_NEW_PREFS = "auth_new_prefs"
    private const val LABEL_AUTH_EMAIL_SIGN_IN = "auth_email_sign_in"
    private const val LABEL_NAVIGATION_BREAKPOINT = "auth_navigation_breakpoint"
    private const val LABEL_USER_ID = "current_user_id"

    fun saveEmailToSignIn(context: Context, email: String) {
        sharedPreferences(context).edit(true) {
            putString(LABEL_AUTH_EMAIL_SIGN_IN, email)
        }
    }

    fun loadEmailToSignIn(context: Context): String? {
        return sharedPreferences(context).getString(LABEL_AUTH_EMAIL_SIGN_IN, null)
    }

    fun saveNavigationBreakPoint(context: Context, breakPoint: String?) {
        sharedPreferences(context).edit(true) {
            putString(LABEL_NAVIGATION_BREAKPOINT, breakPoint)
        }
    }

    fun clearNavigationBreakPoint(context: Context) {
        sharedPreferences(context).edit(true) {
            remove(LABEL_NAVIGATION_BREAKPOINT)
        }
    }

    fun loadNavigationBreakPoint(context: Context): String? {
        return sharedPreferences(context).getString(LABEL_NAVIGATION_BREAKPOINT, null)
    }

    private fun sharedPreferences(context: Context) =
        context.getSharedPreferences(LABEL_AUTH_NEW_PREFS, Context.MODE_PRIVATE)

    fun saveCurrentUserId(context: Context,id: Int){
        sharedPreferences(context).edit(true) {
            putInt(LABEL_USER_ID, id)
        }
    }

    fun getCurrentUserId(context: Context) = sharedPreferences(context).getInt(LABEL_USER_ID, 0)
}