package com.limor.app.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.limor.app.scenes.main.viewmodels.FeedViewModel
import com.limor.app.scenes.utils.CommonsKt
import io.reactivex.subjects.PublishSubject

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
        if(rootView == null) {
            super.onCreateView(inflater, container, savedInstanceState)
            initApiCallGetFeed()
            requestNewData()
        }
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
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })


    }


    override fun resetFeedViewModelVariables() {
        viewModelFeed.limit = FEED_LIMIT_REQUEST
        viewModelFeed.offset = 0
    }

    override fun setFeedViewModelVariablesOnScroll() {
        viewModelFeed.limit = FEED_LIMIT_REQUEST
        viewModelFeed.offset = feedItemsList.size
    }

}