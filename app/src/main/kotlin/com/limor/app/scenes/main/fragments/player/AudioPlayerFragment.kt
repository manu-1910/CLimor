package com.limor.app.scenes.main.fragments.player

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.common.BaseFragment
import com.limor.app.common.SessionManager
import com.limor.app.events.Event
import com.limor.app.scenes.main.adapters.FeedAdapter
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsActivity
import com.limor.app.scenes.main.fragments.podcast.PodcastsByTagActivity
import com.limor.app.scenes.main.viewmodels.*
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.service.AudioService
import com.limor.app.uimodels.UIFeedItem
import com.limor.app.uimodels.UIPodcast
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class AudioPlayerFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sessionManager: SessionManager

    // viewModels
    private lateinit var viewModelFeed: FeedViewModel
    private lateinit var viewModelFeedByTag: FeedByTagViewModel
    private lateinit var viewModelCreatePodcastLike: CreatePodcastLikeViewModel
    private lateinit var viewModelDeletePodcastLike: DeletePodcastLikeViewModel

    private lateinit var viewModelDeletePodcast: DeletePodcastViewModel

    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastDataTrigger = PublishSubject.create<Unit>()


    // views
    private var rootView: View? = null
    private var rvFeed: RecyclerView? = null

    private var feedAdapter: FeedAdapter? = null
    private var feedItemsList: ArrayList<UIFeedItem> = ArrayList()

    // like variables
    private var lastLikedItemPosition: Int = 0

    private var uiPodcast: UIPodcast? = null

    val app: App? by lazy {
        requireContext().applicationContext as App
    }

    companion object {
        val TAG: String = AudioPlayerFragment::class.java.simpleName
        fun newInstance() = AudioPlayerFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_audio_player, container, false)
            rvFeed = rootView?.findViewById(R.id.rvFeed)

            val bundle = requireActivity().intent?.extras
            if (bundle != null && bundle.containsKey(AudioPlayerActivity.BUNDLE_KEY_PODCAST)) {
                uiPodcast =
                        bundle.getSerializable(AudioPlayerActivity.BUNDLE_KEY_PODCAST) as UIPodcast

                // Wrap the podcast with a UIFeedItem so that we can use the feedAdapter here...
                val uiFeedItem = UIFeedItem(
                        uiPodcast!!.id.toString(),
                        uiPodcast,
                        uiPodcast!!.user,
                        false,
                        uiPodcast!!.created_at
                )

                feedItemsList.add(uiFeedItem)
            }

            bindViewModel()
            configureAdapter()
            initApiCallCreateLike()
            initApiCallDeleteLike()
            initApiCallDeletePodcast()
        }
        return rootView
    }

    private fun initApiCallDeletePodcast() {
        val output = viewModelDeletePodcast.transform(DeletePodcastViewModel.Input(deletePodcastDataTrigger))

        output.response.observe(this, Observer { response ->
            val code = response.code
            if (code != 0) {
                toast(getString(R.string.delete_podcast_error))
            } else {
                val activity = requireActivity() as AudioPlayerActivity
                activity.closePlayer()
                EventBus.getDefault().postSticky(Event.RefreshFeed)
            }
        })

        output.errorMessage.observe(this, Observer {
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
            toast(getString(R.string.delete_podcast_error))
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
                            val podcastDetailsIntent =
                                    Intent(context, PodcastDetailsActivity::class.java)
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
                            Toast.makeText(context, "You clicked on recast", Toast.LENGTH_SHORT).show()
                        }

                        override fun onHashtagClicked(hashtag: String) {

                            val podcastByTagIntent = Intent(context, PodcastsByTagActivity::class.java)
                            podcastByTagIntent.putExtra(
                                    PodcastsByTagActivity.BUNDLE_KEY_HASHTAG,
                                    hashtag
                            )
                            startActivity(podcastByTagIntent)

                            val activity = requireActivity() as AudioPlayerActivity
                            activity.finish()

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

                        override fun onMoreClicked(
                                item: UIFeedItem,
                                position: Int,
                                view: View
                        ) {
                            showMorePopupMenu(view, item, position)
                        }
                    },
                    sessionManager,
                    false
            )
        }
        rvFeed?.adapter = feedAdapter

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

    private fun showMorePopupMenu(
            view: View?,
            item: UIFeedItem,
            position: Int
    ) {
        val popup = PopupMenu(context, view, Gravity.TOP)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup_audio_player, popup.menu)

        val loggedUser = sessionManager.getStoredUser()
        if (item.podcast?.user?.id != loggedUser?.id) {
            val menuToHide = popup.menu.findItem(R.id.menu_delete_cast)
            menuToHide.isVisible = false
        } else {
            val menuReportCast = popup.menu.findItem(R.id.menu_report_cast)
            menuReportCast.isVisible = false
        }

        //set menu item click listener here
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_share -> onShareClicked(item)
               // R.id.menu_report_cast -> onPodcastReportClicked(item)
                R.id.menu_delete_cast -> onDeletePodcastClicked(item)
            }
            true
        }
        popup.show()
    }

    private fun onDeletePodcastClicked(item: UIFeedItem) {
        alert(getString(R.string.confirmation_delete_podcast)) {
            okButton {
                viewModelDeletePodcast.podcast = item.podcast
                deletePodcastDataTrigger.onNext(Unit)
            }
            cancelButton { }
        }.show()
    }


    private fun onShareClicked(item: UIFeedItem) {
        item.podcast?.sharing_url?.let { url ->
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
            viewModelDeletePodcast = ViewModelProviders
                    .of(fragmentActivity, viewModelFactory)
                    .get(DeletePodcastViewModel::class.java)
        }
    }


}