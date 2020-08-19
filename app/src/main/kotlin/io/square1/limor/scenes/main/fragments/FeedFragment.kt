package io.square1.limor.scenes.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.adapters.FeedAdapter
import io.square1.limor.scenes.main.viewmodels.FeedViewModel
import io.square1.limor.uimodels.UIFeedItem
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.onRefresh
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
    var swipeRefreshLayout: SwipeRefreshLayout? = null
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
            swipeRefreshLayout = rootView?.findViewById(R.id.swipeRefreshLayout)
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
                feedItemsList,
                object : FeedAdapter.OnFeedClickListener {
                    override fun onItemClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on item", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPlayClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on play", Toast.LENGTH_SHORT).show()
                    }

                    override fun onListenClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on listen", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCommentClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on comment", Toast.LENGTH_SHORT).show()
                    }

                    override fun onLikeClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on like", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRecastClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on recast", Toast.LENGTH_SHORT).show()
                    }

                    override fun onHashtagClicked(hashtag: String) {
                        Toast.makeText(context, "You clicked on $hashtag hashtag", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSendClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on share", Toast.LENGTH_SHORT).show()
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }

                    override fun onUserClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on user", Toast.LENGTH_SHORT).show()
                    }

                    override fun onMoreClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on more", Toast.LENGTH_SHORT).show()
                    }
                }
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
            feedItemsList.clear()
            feedItemsList.addAll(it.data.feed_items)
            rvFeed?.adapter?.notifyDataSetChanged()
            hideSwipeToRefreshProgressBar()
        })
    }

    private fun hideSwipeToRefreshProgressBar() {
        swipeRefreshLayout?.let {
            if(it.isRefreshing) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }
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
        swipeRefreshLayout?.onRefresh {
            getFeedDataTrigger.onNext(Unit)
        }
    }

}