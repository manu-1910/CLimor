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
import io.square1.limor.common.BaseActivity
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.adapters.FeedAdapter
import io.square1.limor.scenes.main.fragments.podcast.PodcastDetailsActivity
import io.square1.limor.scenes.main.fragments.podcast.PodcastsByTagActivity
import io.square1.limor.scenes.main.viewmodels.*
import io.square1.limor.service.AudioService
import io.square1.limor.uimodels.UIFeedItem
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.uiThread
import javax.inject.Inject


class FeedFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // viewModels
    private lateinit var viewModelFeed: FeedViewModel


    private lateinit var viewModelFeedByTag: FeedByTagViewModel
    private lateinit var viewModelCreatePodcastLike: CreatePodcastLikeViewModel
    private lateinit var viewModelDeletePodcastLike: DeletePodcastLikeViewModel
    private lateinit var viewModelCreatePodcastRecast: CreatePodcastRecastViewModel
    private lateinit var viewModelDeletePodcastRecast: DeletePodcastRecastViewModel
    private val getFeedDataTrigger = PublishSubject.create<Unit>()


    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastRecastDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastRecastDataTrigger = PublishSubject.create<Unit>()

    // infinite scroll variables
    private var isScrolling: Boolean = false
    private var isLastPage: Boolean = false
    private var isReloading: Boolean = false
    private var rootView: View? = null


    // views
    private var rvFeed: RecyclerView? = null

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var feedAdapter: FeedAdapter? = null
    var feedItemsList = ArrayList<UIFeedItem>()

    // like variables
    private var lastLikedItemPosition: Int = 0
    private var lastRecastedItemPosition: Int = 0

    private var hashTag: String = ""


    companion object {
        val TAG: String = FeedFragment::class.java.simpleName
        fun newInstance() = FeedFragment()
        private const val OFFSET_INFINITE_SCROLL = 2
        private const val FEED_LIMIT_REQUEST = 2 // this number multiplied by 2 is because there is
                                                 // an error on the limit param in the back side
                                                 // that duplicates the amount of results,
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

            val bundle = requireActivity().intent?.extras
            if (bundle != null && bundle.containsKey(PodcastsByTagActivity.BUNDLE_KEY_HASHTAG)) {
                hashTag = bundle.getString(PodcastsByTagActivity.BUNDLE_KEY_HASHTAG)!!
            }

            bindViewModel()
            configureAdapter()
            initApiCallGetFeed()
            initApiCallCreateLike()
            initApiCallDeleteLike()
            initApiCallCreateRecast()
            initApiCallDeleteRecast()

            getFeedDataTrigger.onNext(Unit)
        }
        return rootView
    }

    private fun initApiCallDeleteRecast() {
        val output = viewModelDeletePodcastRecast.transform(
            DeletePodcastRecastViewModel.Input(
                deletePodcastRecastDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoRecast()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoRecast()
        })
    }

    private fun undoRecast() {
        Toast.makeText(context, getString(R.string.error_recasting_podcast), Toast.LENGTH_SHORT).show()
        val item = feedItemsList[lastRecastedItemPosition]
        item.podcast?.let { podcast ->
            changeItemRecastStatus(
                item,
                lastLikedItemPosition,
                !podcast.liked
            )
        }
    }

    private fun initApiCallCreateRecast() {
        val output = viewModelCreatePodcastRecast.transform(
            CreatePodcastRecastViewModel.Input(
                createPodcastRecastDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoRecast()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoRecast()
        })
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
                        val podcastDetailsIntent = Intent(context, PodcastDetailsActivity::class.java)
                        podcastDetailsIntent.putExtra("podcast", item.podcast)
                        startActivity(podcastDetailsIntent)
                    }

                    override fun onPlayClicked(item: UIFeedItem, position: Int) {

                        item.podcast?.audio?.audio_url?.let { _ ->

                            AudioService.newIntent(requireContext(), item.podcast!!, 1L)
                                .also { intent ->
                                    requireContext().startService(intent)
                                    val activity = requireActivity() as BaseActivity
                                    activity.showMiniPlayer()
                                }

                        }

                    }

                    override fun onListenClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on listen", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCommentClicked(item: UIFeedItem, position: Int) {
                        val podcastDetailsIntent =
                            Intent(context, PodcastDetailsActivity::class.java)
                        podcastDetailsIntent.putExtra("podcast", item.podcast)
                        podcastDetailsIntent.putExtra("commenting", true)
                        startActivity(podcastDetailsIntent)
                    }

                    override fun onLikeClicked(item: UIFeedItem, position: Int) {
                        item.podcast?.let { podcast ->
                            changeItemLikeStatus(
                                item,
                                position,
                                !podcast.liked
                            ) // careful, it will change an item to be from like to dislike and viceversa
                            lastLikedItemPosition = position

                            // if now it's liked, let's call the api
                            if (podcast.liked) {
                                viewModelCreatePodcastLike.idPodcast = podcast.id
                                createPodcastLikeDataTrigger.onNext(Unit)

                                // if now it's not liked, let's call the api
                            } else {
                                viewModelDeletePodcastLike.idPodcast = podcast.id
                                deletePodcastLikeDataTrigger.onNext(Unit)
                            }
                        }
                    }

                    override fun onRecastClicked(item: UIFeedItem, position: Int) {
                        item.podcast?.let { podcast ->
                            changeItemRecastStatus(
                                item,
                                position,
                                !podcast.recasted
                            ) // careful, it will change an item to be from like to dislike and viceversa
                            lastRecastedItemPosition = position

                            // if now it's liked, let's call the api
                            if (podcast.recasted) {
                                viewModelCreatePodcastRecast.idPodcast = podcast.id
                                createPodcastRecastDataTrigger.onNext(Unit)

                                // if now it's not liked, let's call the api
                            } else {
                                viewModelDeletePodcastRecast.idPodcast = podcast.id
                                deletePodcastRecastDataTrigger.onNext(Unit)
                            }
                        }
                    }

                    override fun onHashtagClicked(hashtag: String) {
                        val podcastByTagIntent = Intent(context, PodcastsByTagActivity::class.java)
                        podcastByTagIntent.putExtra(
                            PodcastsByTagActivity.BUNDLE_KEY_HASHTAG,
                            hashtag
                        )
                        startActivity(podcastByTagIntent)
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

                // if we scroll down...
                if (dy > 0) {

                    // those are the items that we have already passed in the list, the items we already saw
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    // this are the items that are currently showing on screen
                    val visibleItemsCount = layoutManager.childCount

                    // this are the total amount of items
                    val totalItemsCount = layoutManager.itemCount

                    // if the past items + the current visible items + offset is greater than the total amount of items, we have to retrieve more data
                    if (isScrolling && !isLastPage && visibleItemsCount + pastVisibleItems + OFFSET_INFINITE_SCROLL >= totalItemsCount) {
                        isScrolling = false
                        setFeedViewModelVariables(feedItemsList.size - 1)
                        getFeedDataTrigger.onNext(Unit)
                    }
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

    private fun changeItemLikeStatus(item: UIFeedItem, position: Int, liked: Boolean) {
        item.podcast?.let { podcast ->
            if (liked) {
                podcast.number_of_likes++
            } else {
                podcast.number_of_likes--
            }
            podcast.liked = liked
            feedAdapter?.notifyItemChanged(position, item)
        }
    }

    private fun changeItemRecastStatus(item: UIFeedItem, position: Int, recasted: Boolean) {
        item.podcast?.let { podcast ->
            if (recasted) {
                podcast.number_of_recasts++
            } else {
                podcast.number_of_recasts--
            }
            item.podcast?.recasted = recasted
            feedAdapter?.notifyItemChanged(position, item)
        }
    }


    private fun initApiCallGetFeed() {

        if (isHashTagFeed()) {
            val output = viewModelFeedByTag.transform(
                FeedByTagViewModel.Input(
                    getFeedDataTrigger,
                    hashTag
                )
            )

            output.response.observe(this, Observer {

                doAsync {

                    /*
                        This is an attempt to reuse the same feed adapter for regular feedItems AND postCast by tag items
                        PodCasts returned from the api do not have a unique id property as String.
                        Currently, the id property of a UIFeedItem is not used, so a wrapper FeedItem is generated here, with the id
                        Set to the Podcast id's string value.
                     */

                    val items = mutableListOf<UIFeedItem>()
                    for (podcast in it.data.podcasts) {
                        val item = UIFeedItem(
                            podcast.id.toString(),
                            podcast,
                            podcast.user,
                            false,
                            podcast.created_at
                        )
                        items.add(item)
                    }

                    uiThread {
                        handleNewFeedData(items)
                    }
                }

            })

            output.errorMessage.observe(this, Observer {
                handleErrorState()
            })


        } else {
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

    }

    private fun handleErrorState() {
        hideSwipeToRefreshProgressBar()
        Toast.makeText(
            context,
            "We couldn't get your feed, please, try again later",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleNewFeedData(items: MutableList<UIFeedItem>) {

        if (isHashTagFeed()) {
            activity?.let { fragmentActivity ->
                if (fragmentActivity is PodcastsByTagActivity) {
                    fragmentActivity.hideLoadingSpinner()
                }
            }
        }

        if (isReloading) {
            feedItemsList.clear()
            rvFeed?.recycledViewPool?.clear()
            isReloading = false
        }

        feedItemsList.addAll(items)
        if (items.size == 0)
            isLastPage = true


        rvFeed?.adapter?.notifyDataSetChanged()
        hideSwipeToRefreshProgressBar()
    }

    private fun initApiCallCreateLike() {
        val output = viewModelCreatePodcastLike.transform(
            CreatePodcastLikeViewModel.Input(
                createPodcastLikeDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoLike()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoLike()
        })
    }

    private fun initApiCallDeleteLike() {
        val output = viewModelDeletePodcastLike.transform(
            DeletePodcastLikeViewModel.Input(
                deletePodcastLikeDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                undoLike()
            }
        })

        output.errorMessage.observe(this, Observer {
            undoLike()
        })
    }

    private fun undoLike() {
        Toast.makeText(context, getString(R.string.error_liking_podcast), Toast.LENGTH_SHORT).show()
        val item = feedItemsList[lastLikedItemPosition]
        item.podcast?.let { podcast ->
            changeItemLikeStatus(
                item,
                lastLikedItemPosition,
                !podcast.liked
            )
        }
    }


    private fun hideSwipeToRefreshProgressBar() {
        swipeRefreshLayout?.let {
            if (it.isRefreshing) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }
    }


    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelFeed = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(FeedViewModel::class.java)
        }

        activity?.let { fragmentActivity ->
            viewModelFeedByTag = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(FeedByTagViewModel::class.java)
        }

        setFeedViewModelVariables()

        activity?.let { fragmentActivity ->
            viewModelCreatePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastLikeViewModel::class.java)
        }

        activity?.let { fragmentActivity ->
            viewModelDeletePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastLikeViewModel::class.java)
        }

        activity?.let { fragmentActivity ->
            viewModelCreatePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastRecastViewModel::class.java)
        }

        activity?.let { fragmentActivity ->
            viewModelDeletePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastRecastViewModel::class.java)
        }
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
        isReloading = true
        setFeedViewModelVariables()
        getFeedDataTrigger.onNext(Unit)
    }

    private fun setFeedViewModelVariables(newOffset: Int = 0) {
        viewModelFeed.limit = FEED_LIMIT_REQUEST
        viewModelFeed.offset = newOffset

        viewModelFeedByTag.limit = FEED_LIMIT_REQUEST
        viewModelFeedByTag.offset = newOffset
    }

    private fun isHashTagFeed(): Boolean {
        return hashTag.isNotEmpty()
    }

}