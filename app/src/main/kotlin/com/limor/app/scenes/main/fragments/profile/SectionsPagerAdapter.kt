package com.limor.app.scenes.main.fragments.profile

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        if(position == 1){
            return UserFollowersFragment.newInstance()
        }else{
            return UserFollowingsFragment.newInstance()
        }
    }


    override fun getPageTitle(position: Int): CharSequence? {
        if(position == 1){
            return "Tab 11"
        }else{
            return "Tab 22"
        }
    }


    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }

}