package com.limor.app.scenes.auth_new.util

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.android.billingclient.api.SkuDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.limor.app.uimodels.UILocations
import com.limor.app.uimodels.UILocationsList
import com.limor.app.util.AppState
import timber.log.Timber
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
    private const val PREFERENCES_SELECTED = "preferences_selected"
    private const val SELECTED_GENDER_ID = "gender_id"
    private const val KEY_ONBOARDING_URL = "unipaas_onboarding_url"
    private const val KEY_ENABLE_SOUNDS = "key_enable_sounds"
    private const val KEY_USER_ID = "one_signal_notification_user_id"
    private const val KEY_USER_NAME = "one_signal_notification_user_name"
    private const val KEY_TAB_ID = "one_signal_notification_tab_id"
    private const val KEY_JUST_LOGGED_IN = "just_logged_in"
    private const val KEY_CAN_SHOW_CATEGORY_SELECTION = "can_show_category_selection"
    private const val KEY_CAN_SHOW_GENDER_SELECTION = "can_show_gender_popup"
    private const val KEY_COMMENT_ID = "comment_id"
    private const val KEY_CHILD_COMMENT_ID = "child_comment_id"
    private const val KEY_COMMENT_CAST_ID = "comment_cast_id"

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

    fun saveCurrentUserId(context: Context, id: Int) {
        sharedPreferences(context).edit(true) {
            putInt(LABEL_USER_ID, id)
        }
    }

    fun getCurrentUserId(context: Context) = sharedPreferences(context).getInt(LABEL_USER_ID, 0)
    fun savePodCastIdOfSharedLink(context: Context, id: Int) {
        sharedPreferences(context).edit(true) {
            putInt(LABEL_CAST_ID, id)
        }
    }

    fun getPodCastIdOfSharedLink(context: Context) =
        sharedPreferences(context).getInt(LABEL_CAST_ID, -1)

    fun saveUserDeviceToken(context: Context, token: String) {
        sharedPreferences(context).edit(true) {
            putString(LABEL_USER_DEVICE_TOKEN, token)
        }
    }

    fun getCurrentUserDeviceToken(context: Context) =
        sharedPreferences(context).getString(LABEL_USER_DEVICE_TOKEN, null)

    fun setAppState(context: Context, appState: AppState) {
        setAppLastState(
            context,
            if (appState.state == AppState.BACKGROUND.state) AppState.BACKGROUND.state else getAppState(
                context
            )
        )
        sharedPreferences(context).edit(true) {
            putInt(LABEL_APP_STATE, appState.state)
        }
    }

    fun getAppState(context: Context): Int = sharedPreferences(context).getInt(
        LABEL_APP_STATE, -1
    )

    fun setAppLastState(context: Context, state: Int) {
        sharedPreferences(context).edit(true) {
            putInt(LABEL_APP_LAST_STATE, state)
        }
    }

    fun getAppLastState(context: Context): Int = sharedPreferences(context).getInt(
        LABEL_APP_LAST_STATE, -1
    )

    fun getLastVerificationId(context: Context) =
        sp(context).getString(KEY_PHONE_VERIFICATION_ID, null)

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

    fun saveBoolean(context: Context, s: String, value: Boolean) {
        sharedPreferences(context).edit().putBoolean(s, value).apply()
    }

    fun getBoolean(context: Context, s: String): Any {
        return sharedPreferences(context).getBoolean(s, false)
    }

    fun getBoolean(context: Context, s: String, defaultValue: Boolean): Boolean {
        return sharedPreferences(context).getBoolean(s, defaultValue)
    }

    fun getSkuDetails(context: Context, s: String): SkuDetails? {
        val skuDetails =
            Gson().fromJson(sharedPreferences(context).getString(s, ""), SkuDetails::class.java)
        Timber.d("Saved get cast $s ---- $skuDetails")
        return skuDetails
    }

    fun saveSkuDetails(context: Context, s: String, v: SkuDetails) {
        Timber.d("Saved Cast product $s --- $v")
        sharedPreferences(context).edit().putString(s, Gson().toJson(v)).apply()
    }

    fun setPreferencesSelected(context: Context, selected: Boolean) {
        sharedPreferences(context).edit().putBoolean(PREFERENCES_SELECTED, selected).apply()
    }

    fun getPreferencesSelected(context: Context): Boolean {
        return sharedPreferences(context).getBoolean(PREFERENCES_SELECTED, false)
    }

    fun setPreferencesScreenOpenedInThisSession(context: Context, selected: Boolean) {
        sharedPreferences(context).edit().putBoolean("PREFERENCES_OPENED", selected).apply()
    }

    fun getPreferencesScreenOpenedInThisSession(context: Context): Boolean {
        return sharedPreferences(context).getBoolean("PREFERENCES_OPENED", false)
    }

    fun hasOnboardingUrl(context: Context) = !getOnboardingUrl(context).isNullOrEmpty()
    fun getOnboardingUrl(context: Context) = sharedPreferences(context).getString(KEY_ONBOARDING_URL, null)
    fun setOnboardingUrl(context: Context, url: String) {
        sharedPreferences(context).edit(true) {
            putString(KEY_ONBOARDING_URL, url)
        }
    }

    fun areSoundsEnabled(context: Context) = getBoolean(context, KEY_ENABLE_SOUNDS, true)
    fun setSoundsEnabled(context: Context, enabled: Boolean) = saveBoolean(context, KEY_ENABLE_SOUNDS, enabled)

    fun saveUserIdFromOneSignalNotification(context: Context, id: Int) {
        sharedPreferences(context).edit(true) {
            putInt(KEY_USER_ID, id)
        }
    }

    fun getUserIdFromOneSignalNotification(context: Context) =
        sharedPreferences(context).getInt(KEY_USER_ID, 0)

    fun saveUserNameFromOneSignalNotification(context: Context, name: String){
        sharedPreferences(context).edit(true) {
            putString(KEY_USER_NAME, name)
        }
    }

    fun saveUserTabIdFromOneSignalNotification(context: Context, tabId: Int){
        sharedPreferences(context).edit(true) {
            putInt(KEY_TAB_ID, tabId)
        }
    }

    fun getUserTabIdFromOneSignalNotification(context: Context) =
        sharedPreferences(context).getInt(KEY_TAB_ID, 0)

    fun getUserNameFromOneSignalNotification(context: Context) =
        sharedPreferences(context).getString(KEY_USER_NAME, "")

    fun saveJustLoggedIn(context: Context, justLoggedIn: Boolean){
        sharedPreferences(context).edit(true) {
            putBoolean(KEY_JUST_LOGGED_IN, justLoggedIn)
        }
    }

    fun getJustLoggedIn(context: Context) =
        sharedPreferences(context).getBoolean(KEY_JUST_LOGGED_IN, false)

    fun setCanShowCategorySelection(context: Context, show: Boolean){
        sharedPreferences(context).edit(true){
            putBoolean(KEY_CAN_SHOW_CATEGORY_SELECTION, show)
        }
    }

    fun canShowCategorySelection(context: Context) =
        sharedPreferences(context).getBoolean(KEY_CAN_SHOW_CATEGORY_SELECTION, false)

    fun setCanShowGenderSelection(context: Context, show: Boolean){
        sharedPreferences(context).edit(true){
            putBoolean(KEY_CAN_SHOW_GENDER_SELECTION, show)
        }
    }

    fun canShowGenderSelection(context: Context) =
        sharedPreferences(context).getBoolean(KEY_CAN_SHOW_GENDER_SELECTION, false)

    fun setChildCommentId(context: Context, id: Int){
        sharedPreferences(context).edit(true){
            putInt(KEY_CHILD_COMMENT_ID, id)
        }
    }

    fun getChildCommentId(context: Context) = sharedPreferences(context).getInt(KEY_CHILD_COMMENT_ID, -1)

    fun setCommentId(context: Context, id: Int){
        sharedPreferences(context).edit(true){
            putInt(KEY_COMMENT_ID, id)
        }
    }

    fun getCommentId(context: Context) = sharedPreferences(context).getInt(KEY_COMMENT_ID, -1)

    fun setCommentCastId(context: Context, id: Int){
        sharedPreferences(context).edit(true){
            putInt(KEY_COMMENT_CAST_ID, id)
        }
    }

    fun getCommentCastId(context: Context) = sharedPreferences(context).getInt(KEY_COMMENT_CAST_ID, -1)

}