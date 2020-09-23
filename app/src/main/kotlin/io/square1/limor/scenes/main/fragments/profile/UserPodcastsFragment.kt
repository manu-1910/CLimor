package io.square1.limor.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import io.square1.limor.scenes.main.fragments.FeedItemsListFragment
import javax.inject.Inject

class UserPodcastsFragment : FeedItemsListFragment() {

    @Inject
    override lateinit var viewModelFactory: ViewModelProvider.Factory


    companion object {
        val TAG: String = UserPodcastsFragment::class.java.simpleName
        fun newInstance() = UserPodcastsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}