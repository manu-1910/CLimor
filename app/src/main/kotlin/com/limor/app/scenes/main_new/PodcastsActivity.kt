package com.limor.app.scenes.main_new

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.FeedItemsQuery
import com.limor.app.R
import com.limor.app.databinding.ActivityPodcastBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.loadImage
import com.limor.app.scenes.main_new.utils.PodcastActivityTransitionHandler
import com.limor.app.scenes.main_new.view_model.PodcastFullPlayerViewModel
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import dagger.android.AndroidInjection
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject


class PodcastsActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PodcastFullPlayerViewModel by viewModels { viewModelFactory }

    private val playerBinder: PlayerBinder = PlayerBinder(this, WeakReference(this))
    private var item: FeedItemsQuery.FeedItem? = null

    lateinit var binding: ActivityPodcastBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        PodcastActivityTransitionHandler.setUpWindowTransition(weakReference)
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        initBinding()
        subscribeToVeiwModel()
        subscribeToPlayerUpdates()
        prepareArgs()
    }

    private fun initBinding() {
        binding = ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.addTransitionListener(
            PodcastActivityTransitionHandler.transitionListener(weakReference)
        )
    }

    val weakReference get() = WeakReference(this)

    private fun subscribeToVeiwModel() {
        model.showPodcastLiveData.observe(this) {
            it?.podcast?.let { podcast ->
                item = it
                bindViews()
                Handler().postDelayed({
                    playerBinder.startPlayPodcast(lifecycleScope, podcast)
                }, 200)
            }
        }
    }

    private fun bindViews() {
        setPodcastGeneralInfo()
        setPodcastOwnerInfo()
        setPodcastCounters()
        setAudioInfo()
        loadImages()
        setOnClicks()
        addTags()
    }

    private fun setPodcastGeneralInfo() {
        binding.tvPodcastTitle.text = item?.podcast?.title ?: ""
        binding.tvPodcastSubtitle.text = item?.podcast?.caption ?: ""
    }

    private fun setPodcastOwnerInfo() {
        binding.tvPodcastUserName.text =
            StringBuilder(item?.podcast?.owner?.first_name ?: "").append("_")
                .append((item?.podcast?.owner?.last_name ?: ""))

        binding.tvPodcastUserSubtitle.text =
            StringBuilder(item?.podcast?.created_at.toString()).append(" ")
                .append(item?.podcast?.address)
    }

    private fun setPodcastCounters() {
        binding.tvPodcastLikes.text = item?.podcast?.number_of_likes?.toString() ?: ""
        binding.tvPodcastRecast.text = item?.podcast?.number_of_recasts?.toString() ?: ""
        binding.tvPodcastComments.text = item?.podcast?.number_of_comments?.toString() ?: ""
        binding.tvPodcastNumberOfListeners.text = item?.podcast?.number_of_listens?.toString() ?: ""
    }

    private fun setAudioInfo() {
        binding.tvRecastPlayMaxPosition.text = item?.podcast?.audio?.duration?.toString() ?: ""
    }

    private fun subscribeToPlayerUpdates() {
        playerBinder.currentPlayPositionLiveData.observe(this) {
            binding.lpiPodcastProgress.progress = it.second
            binding.tvRecastPlayCurrentPosition.text = (it.first ?: 0 / 1000).toString()
        }

        playerBinder.playerStatusLiveData.observe(this) {
            if (it == null) return@observe
            when (it) {
                is PlayerStatus.Cancelled -> Timber.d("Player Canceled")
                is PlayerStatus.Ended -> binding.btnPodcastPlayExtended.setImageResource(R.drawable.ic_player_play)
                is PlayerStatus.Error -> Timber.d("Player Error")
                is PlayerStatus.Other -> Timber.d("Player Other")
                is PlayerStatus.Paused -> binding.btnPodcastPlayExtended.setImageResource(R.drawable.ic_player_play)
                is PlayerStatus.Playing -> binding.btnPodcastPlayExtended.setImageResource(R.drawable.pause)
            }
        }
    }

    private fun loadImages() {
        binding.ivPodcastAvatar.loadCircleImage(
            item?.podcast?.owner?.images?.small_url ?: ""
        )

        binding.ivPodcastBackground.loadImage(
            item?.podcast?.images?.medium_url ?: ""
        )
    }

    private fun setOnClicks() {
        binding.btnPodcastMore.setOnClickListener {
//            val bundle = bundleOf(
//                ArgsConverter.LABEL_DIALOG_REPORT_PODCAST to ArgsConverter.encodeFeedItemAsReportDialogArgs(
//                    item!!
//                )
//            )
//
//            it.findNavController()
//                .navigate(R.id.action_navigation_home_to_dialog_report_podcast, bundle)
        }

        binding.btnPodcastPlayExtended.setOnClickListener {
            item?.podcast?.let {
                playerBinder.playPause(it)
            }
        }

        binding.btnPodcastRewindBack.setOnClickListener {
            item?.podcast?.let {
                playerBinder.rewind(5000L)
            }
        }

        binding.btnPodcastRewindForward.setOnClickListener {
            item?.podcast?.let {
                playerBinder.forward(5000L)
            }
        }
    }

    private fun addTags() {
        item?.podcast?.tags?.caption?.forEach {
            addTagsItems(it)
        }
    }

    private fun addTagsItems(caption: FeedItemsQuery.Caption?) {
        binding.llPodcastTags.removeAllViews()
        AsyncLayoutInflater(this)
            .inflate(R.layout.item_podcast_tag, binding.llPodcastTags) { v, _, _ ->
                (v as TextView).text = StringBuilder("#").append(caption?.tag ?: "")
                binding.llPodcastTags.addView(v)
            }
    }

    private fun prepareArgs() {
        model.setArgs(intent.extras)
    }
}