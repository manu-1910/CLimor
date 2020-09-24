package io.square1.limor.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.reactivex.subjects.PublishSubject
import io.square1.limor.scenes.main.viewmodels.FeedViewModel

class UserFeedFragment : FeedItemsListFragment() {

    private lateinit var viewModelFeed: FeedViewModel
    private val getFeedDataTrigger = PublishSubject.create<Unit>()


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
        initApiCallGetFeed()
        requestNewData()
        return rootView
    }

    override fun bindViewModel() {
        super.bindViewModel()
        activity?.let { fragmentActivity ->
            viewModelFeed = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(FeedViewModel::class.java)
        }
        resetFeedViewModelVariables()
    }

    override fun callTriggerForNewData() {
        getFeedDataTrigger.onNext(Unit)
    }


    private fun initApiCallGetFeed() {

        val output = viewModelFeed.transform(
            FeedViewModel.Input(
                getFeedDataTrigger
            )
        )

        output.response.observe(this, Observer {
            val newItems = it.data.feed_items
            handleNewFeedData(newItems)
        })

        output.errorMessage.observe(this, Observer {
            handleErrorState()
        })


    }


    override fun resetFeedViewModelVariables() {
        viewModelFeed.limit = FEED_LIMIT_REQUEST
        viewModelFeed.offset = 0
    }

    override fun setFeedViewModelVariablesOnScroll() {
        viewModelFeed.limit = FEED_LIMIT_REQUEST
        viewModelFeed.offset = feedItemsList.size / 2 // TODO: check this, with the backend behaviour, this can has unexpected behaviour too
    }

}