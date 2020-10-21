package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.limor.app.scenes.main.fragments.FeedItemsListFragment
import com.limor.app.scenes.main.fragments.UserFeedFragment
import com.limor.app.scenes.main.viewmodels.GetUserLikedPodcastsViewModel
import com.limor.app.uimodels.UIFeedItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_feed.*

class UserLikedPodcastsFragment(private val userID: Int) : FeedItemsListFragment() {

    private lateinit var viewModelGetLikedPodcasts: GetUserLikedPodcastsViewModel
    private val getPodcastsDataTrigger = PublishSubject.create<Unit>()

    companion object {
        val TAG: String = UserFeedFragment::class.java.simpleName
        fun newInstance(newUserId: Int) = UserLikedPodcastsFragment(newUserId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initApiCallGetPodcasts()
        requestNewData()
        return rootView
    }

    override fun bindViewModel() {
        super.bindViewModel()
        activity?.let { fragmentActivity ->
            viewModelGetLikedPodcasts = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetUserLikedPodcastsViewModel::class.java)
        }
        resetFeedViewModelVariables()
    }

    override fun callTriggerForNewData() {
        getPodcastsDataTrigger.onNext(Unit)
    }




    private fun initApiCallGetPodcasts() {
        val output = viewModelGetLikedPodcasts.transform(
            GetUserLikedPodcastsViewModel.Input(
                getPodcastsDataTrigger,
                userID
            )
        )

        output.response.observe(this, Observer {
            pb_loading.visibility = View.GONE
            val newItems = it.data.podcasts

            val items = mutableListOf<UIFeedItem>()
            for (podcast in newItems) {
                val item = UIFeedItem(
                    podcast.id.toString(),
                    podcast,
                    podcast.user,
                    false,
                    podcast.created_at
                )
                items.add(item)
            }
            handleNewFeedData(items)
        })

        output.errorMessage.observe(this, Observer {
            pb_loading.visibility = View.GONE
            handleErrorState()
        })
    }


    override fun resetFeedViewModelVariables() {
        viewModelGetLikedPodcasts.limit = FEED_LIMIT_REQUEST
        viewModelGetLikedPodcasts.offset = 0
    }

    override fun setFeedViewModelVariablesOnScroll() {
        viewModelGetLikedPodcasts.limit = FEED_LIMIT_REQUEST
        viewModelGetLikedPodcasts.offset = feedItemsList.size
    }


}