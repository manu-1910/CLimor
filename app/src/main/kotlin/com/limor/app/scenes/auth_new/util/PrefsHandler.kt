package com.limor.app.scenes.auth_new.util

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.limor.app.uimodels.UILocations
import com.limor.app.uimodels.UILocationsList
import com.limor.app.util.AppState
import java.lang.reflect.Type


object PrefsHandler {

    private const val LABEL_AUTH_NEW_PREFS = "auth_new_prefs"
    private const val LABEL_AUTH_EMAIL_SIGN_IN = "auth_email_sign_in"
    private const val LABEL_NAVIGATION_BREAKPOINT = "auth_navigation_breakpoint"
    private const val LABEL_USER_ID = "current_user_id"
    private const val LABEL_USER_DEVICE_TOKEN = "current_user_device_token"
    private const val LABEL_CAST_ID = "cast_id"
    private const val LABEL_APP_STATE = "app_state"
    private const val LABEL_APP_LAST_STATE = "app_last_state"
    private const val KEY_PHONE_VERIFICATION_ID = "phone_verification_id"
    private const val LABEL_LAST_PLAYED_CAST_ID = "last_played_cast_id"
    private const val LABEL_RECENT_LOCATION = "recent_locations"

    private fun sp(context: Context) = sharedPreferences(context)

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
        setAppLastState(context, if(appState.state == AppState.BACKGROUND.state) AppState.BACKGROUND.state else getAppState(context))
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

    fun getLastVerificationId(context: Context) = sp(context).getString(KEY_PHONE_VERIFICATION_ID, null)
    fun setLastVerificationId(context: Context, id: String?) = sp(context).edit(true) {
        if (id.isNullOrEmpty()) {
            remove(KEY_PHONE_VERIFICATION_ID)
        } else {
            putString(KEY_PHONE_VERIFICATION_ID, id)
        }
    }

    fun saveRecentLocations(context: Context, list: ArrayList<UILocations?>?) {
        val gson = Gson()
        val json: String = gson.toJson(list)
        sharedPreferences(context).edit(true) {
            putString(LABEL_RECENT_LOCATION, json)
        }
    }

    fun getRecentLocations(context: Context): ArrayList<UILocations> {
        val gson = Gson()
        val json = sharedPreferences(context).getString(LABEL_RECENT_LOCATION, null)
        val type: Type = object : TypeToken<ArrayList<UILocations>>() {}.getType()
        return gson.fromJson(json, type) ?: ArrayList()
    }

}