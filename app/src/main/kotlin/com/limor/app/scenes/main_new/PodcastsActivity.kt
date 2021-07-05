package com.limor.app.scenes.main_new

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.FeedItemsQuery
import com.limor.app.R
import com.limor.app.databinding.ActivityPodcastBinding
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.loadImage
import com.limor.app.scenes.auth_new.util.colorStateList
import com.limor.app.scenes.main_new.fragments.FragmentComments
import com.limor.app.scenes.main_new.utils.PodcastActivityTransitionHandler
import com.limor.app.scenes.main_new.view_model.PodcastControlViewModel
import com.limor.app.scenes.main_new.view_model.PodcastFullPlayerViewModel
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject


class PodcastsActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PodcastFullPlayerViewModel by viewModels { viewModelFactory }
    private val controlModel: PodcastControlViewModel by viewModels { viewModelFactory }

    private val playerBinder: PlayerBinder = PlayerBinder(this, WeakReference(this))
    private var podcast: FeedItemsQuery.Podcast? = null

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
            val firstLoad = this.podcast == null
            it?.let { podcast ->
                this.podcast = podcast
                bindViews()
                if (!firstLoad) return@observe
                Handler().postDelayed({
                    playerBinder.startPlayPodcast(lifecycleScope, podcast)
                }, 200)
            }
        }

        controlModel.podcastUpdatedLiveData.observe(this) {
            it?.let {
                // for now, will update the item
                model.getPodcastById(it)
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
        setActiveIcons()
    }

    private fun setPodcastGeneralInfo() {
        binding.tvPodcastTitle.text = podcast?.title ?: ""
        binding.tvPodcastSubtitle.text = podcast?.caption ?: ""
    }

    private fun setPodcastOwnerInfo() {
        binding.tvPodcastUserName.text =
            StringBuilder(podcast?.owner?.first_name ?: "").append("_")
                .append((podcast?.owner?.last_name ?: ""))

        binding.tvPodcastUserSubtitle.text =
            StringBuilder(podcast?.created_at.toString()).append(" ")
                .append(podcast?.address)
    }

    private fun setPodcastCounters() {
        binding.tvPodcastLikes.text = podcast?.number_of_likes?.toString() ?: ""
        binding.tvPodcastRecast.text = podcast?.number_of_recasts?.toString() ?: ""
        binding.tvPodcastComments.text = podcast?.number_of_comments?.toString() ?: ""
        binding.tvPodcastNumberOfListeners.text = podcast?.number_of_listens?.toString() ?: ""
    }

    private fun setAudioInfo() {
        binding.tvRecastPlayMaxPosition.text = podcast?.audio?.duration?.toString() ?: ""
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
            podcast?.owner?.images?.small_url ?: ""
        )

        binding.ivPodcastBackground.loadImage(
            podcast?.images?.medium_url ?: ""
        )
    }

    private fun setOnClicks() {
        binding.btnPodcastMore.setOnClickListener {
        }

        binding.btnPodcastPlayExtended.setOnClickListener {
            podcast?.let {
                playerBinder.playPause(it)
            }
        }

        binding.btnPodcastRewindBack.setOnClickListener {
            podcast?.let {
                playerBinder.rewind(5000L)
            }
        }

        binding.btnPodcastRewindForward.setOnClickListener {
            podcast?.let {
                playerBinder.forward(5000L)
            }
        }

        binding.btnPodcastLikes.setOnClickListener {
            if (podcast?.liked == false)
                controlModel.likePodcast(podcast?.id ?: 0)
            else
                controlModel.unlikePodcast(podcast?.id ?: 0)
        }

        binding.llExtendCommentsHeader.setOnClickListener {
            val bundle = bundleOf(FragmentComments.PODCAST_ID_EXTRA to (podcast?.id ?: 0))
//             Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentCommets) bundle)
            val fragment = FragmentComments()
            fragment.arguments = bundle
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_comments_container, fragment, "FragmentCommentTag")
            }
        }
    }

    private fun addTags() {
        podcast?.tags?.caption?.forEach {
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

    private fun setActiveIcons() {
        val tint = colorStateList(
            binding.root.context,
            if (podcast?.liked == true) R.color.colorAccent else R.color.subtitle_text_color
        )
        binding.btnPodcastLikes.imageTintList = tint
        binding.tvPodcastLikes.setTextColor(tint)
    }

    private fun prepareArgs() {
        model.setArgs(intent.extras)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
}