package com.limor.app.common

import android.content.Context
import com.google.gson.Gson
import com.limor.app.uimodels.UIUser
import io.square1.limor.remote.services.RemoteServiceConfig
import javax.inject.Inject


class SessionManager @Inject constructor(context: Context) {

    @Inject
    lateinit var remoteServiceConfig: RemoteServiceConfig

    private val preferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)

    private val tokenKey = "token_id_key"
    private val userKey = "user_key"
    private val pushTokenKey = "push_token_key"
    private val sessionExpiredIn = "session_expired_in"
    private val pushNotificationsEnabledKey = "push_notifications_enabled_key"


    fun storeToken(tokenId: String): Boolean {
        //Set the token to the RemoteService to send the Authorization header with Bearer token
        remoteServiceConfig.token = tokenId

        return preferences.edit().putString(tokenKey, tokenId).commit()
    }


    fun getStoredToken(): String? {
        return preferences.getString(tokenKey, "")
    }


    fun storePushToken(pushTokenId: String): Boolean {
        return preferences.edit().putString(pushTokenKey, pushTokenId).commit()
    }


    fun getStoredPushToken(): String? {
        return preferences.getString(pushTokenKey, "")
    }


    fun storeUser(uiUser: UIUser): Boolean{
        return preferences.edit().putString(userKey, Gson().toJson(uiUser)).commit()
    }


    fun getStoredUser(): UIUser? {
        return Gson().fromJson(preferences.getString(userKey, ""), UIUser::class.java )
    }

    fun logOut() {
        //Remove token from RemoteServiceConfig to clean up the api calls
        remoteServiceConfig.token = ""

        preferences.edit()
            .remove(tokenKey)
            .remove(userKey)
            .apply()
    }


    fun storeSessionExpiredTime(sessionTime: Long): Boolean {
        remoteServiceConfig.expiredIn = sessionTime

        return preferences.edit().putLong(sessionExpiredIn, sessionTime).commit()
    }

    fun getStoredSessionTime(): Long? {
        return preferences.getLong(sessionExpiredIn, 0)
    }



    fun storePushNotificationsEnabled(enabled: Boolean): Boolean {
        return preferences.edit().putBoolean(pushNotificationsEnabledKey, enabled).commit()
    }


    fun getPushNotificationsEnabled(): Boolean? {
        return preferences.getBoolean(pushNotificationsEnabledKey, false)
    }


}