package io.square1.limor.scenes.main.fragments.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.common.BaseFragment
import io.square1.limor.common.SessionManager
import io.square1.limor.scenes.main.adapters.FeedAdapter
import io.square1.limor.scenes.main.fragments.podcast.PodcastDetailsActivity
import io.square1.limor.scenes.main.fragments.podcast.PodcastsByTagActivity
import io.square1.limor.scenes.main.viewmodels.CreatePodcastLikeViewModel
import io.square1.limor.scenes.main.viewmodels.DeletePodcastLikeViewModel
import io.square1.limor.scenes.main.viewmodels.FeedByTagViewModel
import io.square1.limor.scenes.main.viewmodels.FeedViewModel
import io.square1.limor.service.AudioService
import io.square1.limor.uimodels.UIFeedItem
import io.square1.limor.uimodels.UIPodcast
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

    private val createPodcastLikeDataTrigger = PublishSubject.create<Unit>()
    private val deletePodcastLikeDataTrigger = PublishSubject.create<Unit>()

    // views
    private var rootView: View? = null
    private var rvFeed: RecyclerView? = null

    private var feedAdapter: FeedAdapter? = null
    private var feedItemsList: ArrayList<UIFeedItem> = ArrayList()

    // like variables
    private var lastLikedItemPosition: Int = 0

    private var uiPodcast: UIPodcast? = null

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

                    override fun onMoreClicked(item: UIFeedItem, position: Int) {
                        Toast.makeText(context, "You clicked on more", Toast.LENGTH_SHORT).show()
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
    }


}