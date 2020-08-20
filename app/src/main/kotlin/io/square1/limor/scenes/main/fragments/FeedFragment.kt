package io.square1.limor.scenes.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
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
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.adapters.FeedAdapter
import io.square1.limor.scenes.main.viewmodels.CreatePodcastLikeViewModel
import io.square1.limor.scenes.main.viewmodels.FeedViewModel
import io.square1.limor.uimodels.UIFeedItem
import org.jetbrains.anko.support.v4.onRefresh
import javax.inject.Inject


class FeedFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sessionManager: SessionManager


    // viewModels
    private lateinit var viewModelFeed: FeedViewModel
    private lateinit var viewModelCreatePodcastLike: CreatePodcastLikeViewModel


    private val getFeedDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()


    // infinite scroll variables
    private val FEED_LIMIT_REQUEST =
        2 // this number multiplied by 2 is because there is an error on the limit

    // param in the back side that duplicates the amount of results,
    // to keep it in mind we will multiply by 2. When it's fixed we'll remove it
    private var currentItems: Int = 0
    private var totalItems: Int = 0
    private var isScrolling: Boolean = false
    private var isLastPage: Boolean = false
    private var scrollOutItem: Int = 0


    var rootView: View? = null

    // views
    var rvFeed: RecyclerView? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    var feedAdapter: FeedAdapter? = null
    var feedItemsList: ArrayList<UIFeedItem> = ArrayList()

    // like variables
    private var lastLikedItemPosition: Int = 0


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

            bindViewModel()
            configureAdapter()
            apiCallGetFeed()
            apiCallCreateLike()

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
                        item.podcast?.let { podcast ->
                            changeIconLike(item, position, !podcast.liked)
                            lastLikedItemPosition = position

                            // if it wasn't liked, we have to like it
                            if(!podcast.liked) {
                                viewModelCreatePodcastLike.idPodcast = podcast.id
                                createPodcastLikeDataTrigger.onNext(Unit)

                                // if it was liked, we have to not like it
                            } else {
                                Toast.makeText(context, "TODO: cannot dislike yet", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onRecastClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on recast", Toast.LENGTH_SHORT).show()
                    }

                    override fun onHashtagClicked(hashtag: String) {
                        Toast.makeText(
                            context,
                            "You clicked on $hashtag hashtag",
                            Toast.LENGTH_SHORT
                        ).show()
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
        rvFeed?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = layoutManager.childCount
                totalItems = layoutManager.itemCount

                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItem >= 0) {
                    scrollOutItem = firstVisibleItem
                }

                if (!isLastPage)
                    if (isScrolling && currentItems + scrollOutItem == totalItems) {
                        isScrolling = false
                        // we have to recall the api to get new values
                        setFeedViewModelVariables(feedItemsList.size)
                        getFeedDataTrigger.onNext(Unit)
                    }
            }
        })
        rvFeed?.setHasFixedSize(false)
        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        context?.getDrawable(R.drawable.divider_item_recyclerview)?.let { divider.setDrawable(it) }
        rvFeed?.addItemDecoration(divider)

    }

    private fun changeIconLike(item: UIFeedItem, position: Int, liked: Boolean) {
        item.podcast?.let {podcast ->
            if(liked) {
                podcast.number_of_likes++
            } else {
                podcast.number_of_likes--
            }
            podcast.liked = liked
            feedAdapter?.notifyItemChanged(position, item)
        }
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
            val newItems = it.data.feed_items
            feedItemsList.addAll(newItems)
            if (newItems.size < FEED_LIMIT_REQUEST)
                isLastPage = true
            rvFeed?.adapter?.notifyDataSetChanged()
            hideSwipeToRefreshProgressBar()
        })
    }

    private fun apiCallCreateLike() {
        val output = viewModelCreatePodcastLike.transform(
            CreatePodcastLikeViewModel.Input(
                createPodcastLikeDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                Toast.makeText(context, getString(R.string.error_liking_podcast), Toast.LENGTH_SHORT).show()
                val item = feedItemsList[lastLikedItemPosition]
                item.podcast?.let { podcast -> changeIconLike(item, lastLikedItemPosition, !podcast.liked) }
            }
        })
    }


    private fun hideSwipeToRefreshProgressBar() {
        swipeRefreshLayout?.let {
            if (it.isRefreshing) {
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
        setFeedViewModelVariables()

        activity?.let { fragmentActivity ->
            viewModelCreatePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastLikeViewModel::class.java)
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
            reloadFeed()
        }
    }

    private fun reloadFeed() {
        isLastPage = false
        feedItemsList.clear()
        setFeedViewModelVariables()
        getFeedDataTrigger.onNext(Unit)
    }

    private fun setFeedViewModelVariables(newOffset: Int = 0) {
        viewModelFeed.limit = FEED_LIMIT_REQUEST
        viewModelFeed.offset = newOffset
    }

}