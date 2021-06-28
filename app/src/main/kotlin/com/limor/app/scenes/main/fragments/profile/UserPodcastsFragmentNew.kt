package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.limor.app.R
import com.limor.app.scenes.main.fragments.UserFeedFragment
import timber.log.Timber

class UserPodcastsFragmentNew(): Fragment() {

    companion object {
        val TAG: String = UserFeedFragment::class.java.simpleName
        fun newInstance(newUserId: Int) = UserPodcastsFragmentNew()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("PODCASTS PROFILE")
    }









}