package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.scenes.main.fragments.FeedItemsListFragment
import com.limor.app.scenes.main.fragments.UserFeedFragment
import com.limor.app.scenes.main.viewmodels.GetUserLikedPodcastsViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UIFeedItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_empty_scenario.*
import kotlinx.android.synthetic.main.fragment_feed.*
import org.jetbrains.anko.sdk23.listeners.onClick

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showEmptyScenario(true)
        listeners()
    }

    private fun listeners() {
        if (userID == sessionManager.getStoredUser()?.id) {
            tvActionEmptyScenario?.onClick {
                findNavController().navigate(R.id.navigation_discover)
            }
        }
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
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
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

    override fun showEmptyScenario(show: Boolean) {
        if (show) {
            layEmptyScenario?.visibility = View.VISIBLE
            context?.let {
                ivEmptyScenario.setImageDrawable(
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.like_icon_empty_scenario
                    )
                )
            }
            tvTitleEmptyScenario.text = getString(R.string.likes)

            if (userID == sessionManager.getStoredUser()?.id) {
                tvDescriptionEmptyScenario.text =
                    getString(R.string.empty_scenario_self_liked_casts_description)
                tvActionEmptyScenario.text = getString(R.string.explore)
                tvActionEmptyScenario.visibility = View.VISIBLE
            } else {
                tvDescriptionEmptyScenario.text =
                    getString(R.string.empty_scenario_liked_cast_description)
                tvActionEmptyScenario.visibility = View.GONE
            }

        } else {
            layEmptyScenario?.visibility = View.GONE
        }
    }


}