package com.limor.app.scenes.auth_new.util

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrefsHandler {

    private const val LABEL_AUTH_NEW_PREFS = "auth_new_prefs"
    private const val LABEL_AUTH_EMAIL_SIGN_IN = "auth_email_sign_in"
    private const val LABEL_NAVIGATION_BREAKPOINT = "auth_navigation_breakpoint"

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

    fun loadNavigationBreakPoint(context: Context): String? {
        return sharedPreferences(context).getString(LABEL_NAVIGATION_BREAKPOINT, null)
    }

    suspend fun loadNavigationBreakPointSuspend(context: Context): String? {
       return withContext(Dispatchers.IO){
           loadNavigationBreakPoint(context)
       }
    }

    private fun sharedPreferences(context: Context) =
        context.getSharedPreferences(LABEL_AUTH_NEW_PREFS, Context.MODE_PRIVATE)
}