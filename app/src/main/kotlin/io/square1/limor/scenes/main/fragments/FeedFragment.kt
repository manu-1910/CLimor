package io.square1.limor.scenes.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.adapters.FeedAdapter
import io.square1.limor.scenes.main.viewmodels.FeedViewModel
import io.square1.limor.uimodels.UIFeedItem
import javax.inject.Inject


class FeedFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelFeed: FeedViewModel


    @Inject
    lateinit var sessionManager: SessionManager

    private val getFeedDataTrigger = PublishSubject.create<Unit>()

    var app: App? = null

    var rootView: View? = null

    var rvFeed: RecyclerView? = null
    var feedAdapter: FeedAdapter? = null
    var feedItemsList: ArrayList<UIFeedItem> = ArrayList()


    companion object {
        val TAG: String = FeedFragment::class.java.simpleName
        fun newInstance() = FeedFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_feed, container, false)
            rvFeed = rootView?.findViewById(R.id.rvFeed)
            app = context?.applicationContext as App

            bindViewModel()
            configureAdapter()
            apiCallGetFeed()

            getFeedDataTrigger.onNext(Unit)
        }
        return rootView
    }

    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvFeed?.layoutManager = layoutManager
        feedAdapter = context?.let {
            FeedAdapter(
                it,
                feedItemsList
            )
        }
        rvFeed?.adapter = feedAdapter
        rvFeed?.setHasFixedSize(false)
        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        context?.getDrawable(R.drawable.divider_item_recyclerview)?.let { divider.setDrawable(it) }
        rvFeed?.addItemDecoration(divider)

    }


    private fun apiCallGetFeed() {
        val output = viewModelFeed.transform(
            FeedViewModel.Input(
                getFeedDataTrigger
            )
        )

        output.response.observe(this, Observer {
//            System.out.println("Hemos obtenido datos del feeddddddd")
//            System.out.println("Hemos obtenido ${it.data.feed_items.size} items")
            feedItemsList.addAll(it.data.feed_items)
            rvFeed?.adapter?.notifyDataSetChanged()
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelFeed = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(FeedViewModel::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 20f)
    }

}