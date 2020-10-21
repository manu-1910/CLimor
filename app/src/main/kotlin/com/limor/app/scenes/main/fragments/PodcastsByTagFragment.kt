package com.limor.app.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.limor.app.scenes.main.fragments.podcast.PodcastsByTagActivity
import com.limor.app.scenes.main.viewmodels.FeedByTagViewModel
import com.limor.app.uimodels.UIFeedItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_feed.*

class PodcastsByTagFragment : FeedItemsListFragment() {

    private lateinit var viewModelFeedByTag: FeedByTagViewModel
    private val getFeedDataTrigger = PublishSubject.create<Unit>()

    private var hashtag: String = ""

    companion object {
        val TAG: String = UserFeedFragment::class.java.simpleName
        fun newInstance() = UserFeedFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val bundle = requireActivity().intent?.extras
        bundle?.let {
            if (it.containsKey(PodcastsByTagActivity.BUNDLE_KEY_HASHTAG))
                hashtag = bundle.getString(PodcastsByTagActivity.BUNDLE_KEY_HASHTAG)!!
        }
        initApiCallGetPodcastsByTag()
        requestNewData()
        return rootView
    }

    override fun bindViewModel() {
        super.bindViewModel()
        activity?.let { fragmentActivity ->
            viewModelFeedByTag = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(FeedByTagViewModel::class.java)
        }
        resetFeedViewModelVariables()
    }

    override fun callTriggerForNewData() {
        getFeedDataTrigger.onNext(Unit)
    }


    private fun initApiCallGetPodcastsByTag() {
        val output = viewModelFeedByTag.transform(
            FeedByTagViewModel.Input(
                getFeedDataTrigger,
                hashtag
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
        viewModelFeedByTag.limit = FEED_LIMIT_REQUEST
        viewModelFeedByTag.offset = 0
    }

    override fun setFeedViewModelVariablesOnScroll() {
        viewModelFeedByTag.limit = FEED_LIMIT_REQUEST
        viewModelFeedByTag.offset = feedItemsList.size
    }

}