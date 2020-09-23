package io.square1.limor.scenes.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.PopupMenu
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
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.adapters.FeedAdapter
import io.square1.limor.scenes.main.fragments.podcast.PodcastDetailsActivity
import io.square1.limor.scenes.main.fragments.podcast.PodcastsByTagActivity
import io.square1.limor.scenes.main.fragments.profile.UserProfileActivity
import io.square1.limor.scenes.main.viewmodels.*
import io.square1.limor.service.AudioService
import io.square1.limor.uimodels.UIFeedItem
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


open class FeedItemsListFragment : BaseFragment() {



    @Inject
    open lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var viewModelCreatePodcastLike: CreatePodcastLikeViewModel
    private lateinit var viewModelDeletePodcastLike: DeletePodcastLikeViewModel
    private lateinit var viewModelCreatePodcastRecast: CreatePodcastRecastViewModel
    private lateinit var viewModelDeletePodcastRecast: DeletePodcastRecastViewModel
    private lateinit var viewModelCreatePodcastReport: CreatePodcastReportViewModel

    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastRecastDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastRecastDataTrigger = PublishSubject.create<Unit>()
    private val createPodcastReportDataTrigger = PublishSubject.create<Unit>()

    // infinite scroll variables
    private var isScrolling: Boolean = false
    protected var isLastPage: Boolean = false
    protected var isReloading: Boolean = false
    protected var rootView: View? = null


    // views
    protected var rvFeed: RecyclerView? = null

    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var feedAdapter: FeedAdapter? = null
    var feedItemsList = ArrayList<UIFeedItem>()

    // like variables
    private var lastLikedItemPosition: Int = 0
    private var lastRecastedItemPosition: Int = 0


    companion object {
        val TAG: String = FeedItemsListFragment::class.java.simpleName
        fun newInstance() = FeedItemsListFragment()
        private const val OFFSET_INFINITE_SCROLL = 2
        internal const val FEED_LIMIT_REQUEST = 2 // this number multiplied by 2 is because there is
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

            bindViewModel()
            configureAdapter()
            initApiCallCreateLike()
            initApiCallDeleteLike()
            initApiCallCreateRecast()
            initApiCallDeleteRecast()
            initApiCallCreatePodcastReport()

            requestNewData()
        }
        return rootView
    }

    protected open fun requestNewData() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 20f)
        swipeRefreshLayout?.onRefresh {
            reloadFeed()
        }
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
                        Toast.makeText(context, "You clicked on send", Toast.LENGTH_SHORT).show()
                    }

                    override fun onUserClicked(item: UIFeedItem, position: Int) {
                        val userProfileIntent = Intent(context, UserProfileActivity::class.java)
                        userProfileIntent.putExtra("user", item.podcast?.user)
                        startActivity(userProfileIntent)
                    }

                    override fun onMoreClicked(
                        item: UIFeedItem,
                        position: Int,
                        view: View
                    ) {
                        showPopupMenu(view, item)
                    }
                },
                sessionManager,
                true
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
                        requestNewData()
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

    private fun showPopupMenu(
        view: View?,
        item: UIFeedItem
    ) {
        val popup = PopupMenu(context, view, Gravity.TOP)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_podcast, popup.menu)


        //set menu item click listener here
        popup.setOnMenuItemClickListener {menuItem ->
            when(menuItem.itemId) {
                R.id.menu_share -> onShareClicked(item)
                R.id.menu_report -> onPodcastReportClicked(item)
            }
            true
        }
        popup.show()
    }

    private fun onPodcastReportClicked(item: UIFeedItem) {
        item.podcast?.id?.let {
            viewModelCreatePodcastReport.idPodcastToReport = it
            createPodcastReportDataTrigger.onNext(Unit)
        }
    }

    private fun onShareClicked(item: UIFeedItem) {
        item.podcast?.sharing_url?.let {url ->
            val text = getString(R.string.check_out_this_cast)
            val finalText = "$text $url"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, finalText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        } ?: run {
            toast(getString(R.string.error_retrieving_sharing_url))
        }
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


    private fun initApiCallCreatePodcastReport() {
        val output = viewModelCreatePodcastReport.transform(
            CreatePodcastReportViewModel.Input(
                createPodcastReportDataTrigger
            )
        )

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                Toast.makeText(
                    context,
                    getString(R.string.podcast_already_reported),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.podcast_reported_ok),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        output.errorMessage.observe(this, Observer {
            Toast.makeText(
                context,
                getString(R.string.error_report),
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    protected fun handleErrorState() {
        hideSwipeToRefreshProgressBar()
        Toast.makeText(
            context,
            "We couldn't get your feed, please, try again later",
            Toast.LENGTH_SHORT
        ).show()
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


    protected fun hideSwipeToRefreshProgressBar() {
        swipeRefreshLayout?.let {
            if (it.isRefreshing) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }
    }


    protected open fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelCreatePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastLikeViewModel::class.java)

            viewModelDeletePodcastLike = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastLikeViewModel::class.java)

            viewModelCreatePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastRecastViewModel::class.java)

            viewModelDeletePodcastRecast = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(DeletePodcastRecastViewModel::class.java)

            viewModelCreatePodcastReport = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CreatePodcastReportViewModel::class.java)
        }
    }


    private fun reloadFeed() {
        isLastPage = false
        isReloading = true
        setFeedViewModelVariables()
        requestNewData()
    }

    protected open fun setFeedViewModelVariables(newOffset: Int = 0) {

    }


    fun scrollToTop() {
        rvFeed?.scrollToPosition(0)
    }

}