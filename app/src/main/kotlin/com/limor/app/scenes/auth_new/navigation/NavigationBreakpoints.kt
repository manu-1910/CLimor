package com.limor.app.scenes.auth_new.navigation

import android.app.Activity
import androidx.annotation.IdRes
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.limor.app.R

enum class NavigationBreakpoints(val destination: String, @IdRes val actionId: Int) {
    ACCOUNT_CREATION("ACCOUNT_CREATION", R.id.fragment_new_auth_enter_email),
    USERNAME_CREATION("USERNAME_CREATION", R.id.fragment_new_auth_enter_username),
    DOB_PHONE_COLLECTION("DOB_PHONE_COLLECTION", R.id.fragment_new_auth_dob_picker),
    PREFERENCE_COLLECTION("PREFERENCE_COLLECTION", R.id.fragment_new_auth_gender),
    SHOW_PROFILES("SHOW_SUGGESTED_PROFILES", R.id.fragment_new_auth_suggested_people),
    ONBOARDING_COMPLETION("ONBOARDING_COMPLETION", R.id.fragment_new_auth_onboarding),
    HOME_FEED("HOMEFEED_DISPLAY", R.id.destination_main_activity)
}

object AuthNavigator {

    fun navigateToFragmentByNavigationBreakpoints(activity: Activity, destination: String) {
        val breakpoint =
            NavigationBreakpoints.values().firstOrNull { it.destination == destination }
                ?: NavigationBreakpoints.ACCOUNT_CREATION
        navigateToFragmentWithClear(activity, breakpoint.actionId)
        if (breakpoint == NavigationBreakpoints.HOME_FEED)
            activity.finish()
    }

    private fun navigateToFragmentWithClear(activity: Activity, @IdRes actionId: Int) {
        val navController = Navigation.findNavController(activity, R.id.nav_host_fragment)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.sign_new_navigation, true)
            .setEnterAnim(R.anim.slide_in_right_enter_no_alpha)
            .setExitAnim(R.anim.slide_out_left_exit_no_alpha)
            .build()
        navController.navigate(actionId, null, navOptions)
    }
}