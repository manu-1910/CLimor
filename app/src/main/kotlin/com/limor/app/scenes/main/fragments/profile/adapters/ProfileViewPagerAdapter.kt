package com.limor.app.scenes.main.fragments.profile.adapters

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.limor.app.scenes.main.fragments.profile.FragmentPurchases
import com.limor.app.scenes.main.fragments.profile.UserPatronFragmentNew
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsFragmentNew
import com.limor.app.uimodels.UserUIModel
import timber.log.Timber

class ProfileViewPagerAdapter(
    private val user: UserUIModel,
    @NonNull fragmentManager: FragmentManager,
    lifecycleOwner: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycleOwner) {

    override fun getItemCount(): Int {
        return if(user.isPatron == true) 3 else 2
    }

    override fun createFragment(position: Int): Fragment {
        Timber.d("Current User $user")
        return when (position) {
            0 -> UserPodcastsFragmentNew.newInstance(user)
            1 -> UserPatronFragmentNew.newInstance(user)
            else -> FragmentPurchases.newInstance(user)
        }
    }
}