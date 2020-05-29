package io.square1.limor.common

import android.content.Context
import io.square1.limor.remote.services.RemoteServiceConfig
import javax.inject.Inject


class SessionManager @Inject constructor(context: Context) {

    @Inject
    lateinit var remoteServiceConfig: RemoteServiceConfig


    private val preferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)

    private val tokenKey = "token_id_key"

    private val userIdKey = "user_id_key"
    private val userEmailKey = "user_email_key"
    private val userNameKey = "user_name_key"
    private val userFirstNameKey = "user_first_name_key"
    private val userLastNameKey = "user_last_name_key"
    private val firstRun = "first_run"


    fun storeToken(tokenId: String): Boolean {
        //Set the token to the RemoteService to send the Authorization header with Bearer token
        remoteServiceConfig.token = tokenId
        return preferences.edit().putString(tokenKey, tokenId).commit()
    }

    fun getStoredSession(): String? {
        return preferences.getString(tokenKey, "")
    }









    fun storeUserId(userId: String): Boolean {
        return preferences.edit().putString(userIdKey, userId).commit()
    }

    fun getStoredUserId(): String? {
        return preferences.getString(userIdKey, null)
    }

    fun setIsFirstRun(isFirstRun: Boolean): Boolean {
        return preferences.edit().putBoolean(firstRun, isFirstRun).commit()
    }

    fun getIsFirstRun(): Boolean {
        return preferences.getBoolean(firstRun, true)
    }

    fun logOut() {
        preferences.edit()
            .remove(tokenKey)
            .remove(userIdKey)
            .remove(userEmailKey)
            .remove(userNameKey)
            .remove(userFirstNameKey)
            .remove(userLastNameKey)
            .apply()
    }

    fun storeUserEmail(userEmail: String): Boolean {
        return preferences.edit().putString(userEmailKey, userEmail).commit()
    }

    fun getStoredUserEmail(): String? {
        return preferences.getString(userEmailKey, null)
    }

    fun storeUserName(userName: String): Boolean {
        return preferences.edit().putString(userNameKey, userName).commit()
    }

    fun getStoredUserName(): String? {
        return preferences.getString(userNameKey, null)
    }

    fun storeUserFirstName(userFirstName: String): Boolean {
        return preferences.edit().putString(userFirstNameKey, userFirstName).commit()
    }

    fun getStoredUserFirstName(): String? {
        return preferences.getString(userFirstNameKey, null)
    }

    fun storeUserLastName(userLastName: String): Boolean {
        return preferences.edit().putString(userLastNameKey, userLastName).commit()
    }

    fun getStoredUserLastName(): String? {
        return preferences.getString(userLastNameKey, null)
    }

}