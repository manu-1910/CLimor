package com.limor.app.scenes.main.fragments.profile.adapters

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.limor.app.scenes.main.fragments.profile.UserPatronFragmentNew
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsFragmentNew

class ProfileViewPagerAdapter(
    private val userId: Int,
    @NonNull fragmentManager: FragmentManager,
    lifecycleOwner: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycleOwner) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserPodcastsFragmentNew.newInstance(userId)
            else -> UserPatronFragmentNew()
        }
    }
}