package com.limor.app.scenes.auth_new.util

import android.content.Context
import androidx.core.content.edit
import com.limor.app.util.AppState

object PrefsHandler {

    private const val LABEL_AUTH_NEW_PREFS = "auth_new_prefs"
    private const val LABEL_AUTH_EMAIL_SIGN_IN = "auth_email_sign_in"
    private const val LABEL_NAVIGATION_BREAKPOINT = "auth_navigation_breakpoint"
    private const val LABEL_USER_ID = "current_user_id"
    private const val LABEL_USER_DEVICE_TOKEN = "current_user_device_token"
    private const val LABEL_CAST_ID = "cast_id"
    private const val LABEL_APP_STATE = "app_state"
    private const val LABEL_APP_LAST_STATE = "app_last_state"

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
    fun savePodCastIdOfSharedLink(context: Context,id: Int){
        sharedPreferences(context).edit(true) {
            putInt(LABEL_CAST_ID, id)
        }
    }

    fun getPodCastIdOfSharedLink(context: Context) = sharedPreferences(context).getInt(LABEL_CAST_ID, 0)

    fun saveUserDeviceToken(context: Context, token: String) {
        sharedPreferences(context).edit(true) {
            putString(LABEL_USER_DEVICE_TOKEN, token)
        }
    }
    fun getCurrentUserDeviceToken(context: Context) = sharedPreferences(context).getString(LABEL_USER_DEVICE_TOKEN, null)

    fun setAppState(context: Context, appState: AppState){
        setAppLastState(context, getAppState(context))
        sharedPreferences(context).edit(true) {
            putInt(LABEL_APP_STATE, appState.state)
        }
    }

    fun getAppState(context: Context): Int = sharedPreferences(context).getInt(
        LABEL_APP_STATE, -1)

    fun setAppLastState(context: Context, state: Int){
        sharedPreferences(context).edit(true) {
            putInt(LABEL_APP_LAST_STATE, state)
        }
    }

    fun getAppLastState(context: Context): Int = sharedPreferences(context).getInt(
        LABEL_APP_LAST_STATE, -1)

}