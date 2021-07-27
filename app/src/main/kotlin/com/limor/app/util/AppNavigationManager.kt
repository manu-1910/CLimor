package com.limor.app.util

import android.content.Context
import android.content.Intent
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.main_new.fragments.ExtendedPlayerFragment
import org.json.JSONObject

object AppNavigationManager {

    const val CAST_KEY = "CAST_KEY"

    fun navigateToUserProfileIntent(context:Context,data:JSONObject):Intent{
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY,"User Profile")
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY,data.getInt("id"))
        return userProfileIntent
    }

    fun navigateToExtendedPlayerIntent(context:Context,data:JSONObject): Intent{
        val userProfileIntent = Intent(context, MainActivityNew::class.java)
        userProfileIntent.putExtra(CAST_KEY,data.getInt("id"))
        return userProfileIntent
    }

    fun navigateToTestProfile(context: Context,i: Int): Intent? {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY,"User Profile")
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY,i)
        return userProfileIntent
    }

}