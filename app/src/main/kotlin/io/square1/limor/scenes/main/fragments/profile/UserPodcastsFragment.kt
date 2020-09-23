package io.square1.limor.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.square1.limor.scenes.main.fragments.FeedItemsListFragment
import timber.log.Timber

class UserPodcastsFragment : FeedItemsListFragment() {


    companion object {
        val TAG: String = UserPodcastsFragment::class.java.simpleName
        fun newInstance() = UserPodcastsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun bindViewModel() {
        super.bindViewModel()
        Timber.d("Acabo de correr bindViewModel del UserPodcastsFragment")
    }

}