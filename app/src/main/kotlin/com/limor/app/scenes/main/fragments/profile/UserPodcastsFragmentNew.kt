package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.scenes.main.fragments.FeedItemsListFragment
import com.limor.app.scenes.main.fragments.UserFeedFragment
import com.limor.app.scenes.main.viewmodels.GetPodcastsByUserIDViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIFeedItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_empty_scenario.*
import kotlinx.android.synthetic.main.fragment_feed.*
import org.jetbrains.anko.sdk23.listeners.onClick

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
        val rootView = inflater.inflate(R.layout.fragment_feed, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }









}