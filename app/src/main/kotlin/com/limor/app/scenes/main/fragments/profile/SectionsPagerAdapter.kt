package com.limor.app.scenes.main.fragments.profile

import android.content.Context
import android.os.Parcelable
import android.provider.Settings.Global.getString
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.limor.app.FollowersQuery
import com.limor.app.R
import com.limor.app.uimodels.UIUser


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, uiUser: UIUser) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val user = uiUser

    val names = arrayOf(
        context.getString(R.string.followers),
        context.getString(R.string.followings)
    )

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position) {
            0 -> UserFollowersFragment.newInstance(user)
            1 -> UserFollowingsFragment.newInstance(user)
            else -> UserFollowersFragment.newInstance(user)
        }

    }


    override fun getPageTitle(position: Int): CharSequence {
        return names[position]
    }


    override fun getCount(): Int {
        return names.size
    }


    // this is necessary. Without this, app will crash when you are in a different fragment
    // and then push back and it goes back to this fragment.
    // the fragmentstatepageradapter saves states between different fragments of the adapter itself
    // but if you go to a different fragment, for example home, and the push back and the navigation
    // goes back to this profile fragment, the fragmentstatepageradapter will try to restore the
    // state of the adapter fragments but they are not alive anymore.
    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        try {
            super.restoreState(state, loader)
        } catch (e: Exception) {
            println("Error Restore State of Fragment : %s"+ e.message)
        }
    }

}