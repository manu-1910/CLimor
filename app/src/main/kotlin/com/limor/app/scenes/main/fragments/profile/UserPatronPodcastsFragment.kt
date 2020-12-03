package com.limor.app.scenes.main.fragments.profile

import com.limor.app.scenes.main.fragments.FeedItemsListFragment
import com.limor.app.scenes.main.viewmodels.GetUserLikedPodcastsViewModel
import io.reactivex.subjects.PublishSubject

class UserPatronPodcastsFragment(private val userID: Int) : FeedItemsListFragment() {


    private lateinit var viewModelGetLikedPodcasts: GetUserLikedPodcastsViewModel
    private val getPodcastsDataTrigger = PublishSubject.create<Unit>()

    companion object {
        val TAG: String = UserPatronPodcastsFragment::class.java.simpleName
        fun newInstance(newUserId: Int) = UserPatronPodcastsFragment(newUserId)
    }


    override fun callTriggerForNewData() {

    }

    override fun setFeedViewModelVariablesOnScroll() {

    }

    override fun showEmptyScenario(show: Boolean) {

    }

    override fun resetFeedViewModelVariables() {

    }

}