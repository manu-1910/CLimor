package io.square1.limor.scenes.main.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.reactivex.subjects.PublishSubject
import io.square1.limor.scenes.main.fragments.FeedItemsListFragment
import io.square1.limor.scenes.main.fragments.UserFeedFragment
import io.square1.limor.scenes.main.viewmodels.GetPodcastsByUserIDViewModel
import io.square1.limor.uimodels.UIFeedItem
import kotlinx.android.synthetic.main.fragment_feed.*

class UserPodcastsFragment(private val userID: Int) : FeedItemsListFragment() {

    private lateinit var viewModelPodcastsByUserID: GetPodcastsByUserIDViewModel
    private val getPodcastsByUserIDDataTrigger = PublishSubject.create<Unit>()

    companion object {
        val TAG: String = UserFeedFragment::class.java.simpleName
        fun newInstance(newUserId: Int) = UserPodcastsFragment(newUserId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initApiCallGetPodcastsByTag()
        requestNewData()
        return rootView
    }

    override fun bindViewModel() {
        super.bindViewModel()
        activity?.let { fragmentActivity ->
            viewModelPodcastsByUserID = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(GetPodcastsByUserIDViewModel::class.java)
        }
        setFeedViewModelVariables()
    }

    override fun callTriggerForNewData() {
        getPodcastsByUserIDDataTrigger.onNext(Unit)
    }


    private fun initApiCallGetPodcastsByTag() {
        val output = viewModelPodcastsByUserID.transform(
            GetPodcastsByUserIDViewModel.Input(
                getPodcastsByUserIDDataTrigger,
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


    override fun setFeedViewModelVariables(newOffset: Int) {
        viewModelPodcastsByUserID.limit = FEED_LIMIT_REQUEST
        viewModelPodcastsByUserID.offset = newOffset
    }


}