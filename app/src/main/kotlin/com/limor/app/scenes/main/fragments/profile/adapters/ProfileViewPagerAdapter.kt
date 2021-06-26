package com.limor.app.scenes.main.fragments.profile.adapters

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.limor.app.scenes.main.fragments.profile.UserFollowersFragment
import com.limor.app.scenes.main.fragments.profile.UserPatronFragmentNew
import com.limor.app.scenes.main.fragments.profile.UserPodcastsFragmentNew

class ProfileViewPagerAdapter(@NonNull fragmentManager: FragmentManager, lifecycleOwner: Lifecycle) :
    FragmentStateAdapter(fragmentManager,lifecycleOwner) {

    private val arrayList: ArrayList<Fragment> = ArrayList()


    override fun getItemCount(): Int {
       return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> UserPodcastsFragmentNew()
            else -> UserPatronFragmentNew()
        }
    }
}