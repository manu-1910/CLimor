package com.limor.app.util

import android.content.Context
import android.content.Intent
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.MainActivityNew
import org.json.JSONObject

object AppNavigationManager {

    const val CAST_KEY = "CAST_KEY"

    fun navigateToUserProfileIntent(context:Context,data:JSONObject):Intent{
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY,data.getString("initiatorUsername"))
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY,data.getString("initiatorId"))
        if (data.has("notificationType") && data.getString("notificationType") == "patronRequest") {
            userProfileIntent.putExtra(UserProfileFragment.TAB_POS, 1)
        } else {
            userProfileIntent.putExtra(UserProfileFragment.TAB_POS, 0)
        }
        return userProfileIntent
    }

    fun navigateToExtendedPlayerIntent(context:Context,data:JSONObject): Intent{
        val userProfileIntent = Intent(context, MainActivityNew::class.java)
        userProfileIntent.putExtra(CAST_KEY,data.getString("targetId").toInt())
        return userProfileIntent
    }

    fun navigateToTestProfile(context: Context,i: Int): Intent? {
        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
        userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY,"")
        userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY,i)
        return userProfileIntent
    }

}