package com.limor.app.scenes.main_new.fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.limor.app.FeedItemsQuery
import com.limor.app.R
import com.limor.app.databinding.FragmentHomeNewBinding
import com.limor.app.di.Injectable
import com.limor.app.extensions.loadCircleImage
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.main_new.PodcastsActivity
import com.limor.app.scenes.main_new.adapters.HomeFeedAdapter
import com.limor.app.scenes.main_new.view.MarginItemDecoration
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.main_new.view_model.PodcastMiniPlayerViewModel
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import kotlinx.android.synthetic.main.fragment_home_new.*
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class FragmentHomeNew : FragmentWithLoading(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val homeFeedViewModel: HomeFeedViewModel by viewModels { viewModelFactory }
    private val podcastPlayerViewModel: PodcastMiniPlayerViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmentHomeNewBinding
    private lateinit var playerBinder: PlayerBinder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initPlayerBinder()
        subscribeToPlayerUpdates()
        binding = FragmentHomeNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initPlayerBinder() {
        playerBinder = PlayerBinder(this, WeakReference(requireContext()))
    }

    private fun subscribeToPlayerUpdates() {
        playerBinder.currentPlayPositionLiveData.observe(viewLifecycleOwner) {
            binding.includeMiniPlayer.cpiPodcastListeningProgress.progress = it.second
            binding.includeMiniPlayer.tvMiniplayerSubtitle.text =
                StringBuilder((it.first ?: 0 / 1000).toString()).append(" ")
                    .append(getString(R.string.left))
        }

        playerBinder.playerStatusLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            when (it) {
                is PlayerStatus.Cancelled -> changePodcastMiniPlayerVisibility(false)
                is PlayerStatus.Ended -> binding.includeMiniPlayer.btnMiniPlayerPlay.setImageResource(
                    R.drawable.ic_player_play
                )
                is PlayerStatus.Error -> Timber.d("Player Error")
                is PlayerStatus.Other -> Timber.d("Player Other")
                is PlayerStatus.Paused -> binding.includeMiniPlayer.btnMiniPlayerPlay.setImageResource(
                    R.drawable.ic_player_play
                )
                is PlayerStatus.Playing -> binding.includeMiniPlayer.btnMiniPlayerPlay.setImageResource(
                    R.drawable.pause
                )
            }
        }
    }

    override fun load() {
        homeFeedViewModel.loadHomeFeed()
    }

    override val errorLiveData: LiveData<String>
        get() = homeFeedViewModel.homeFeedErrorLiveData

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        homeFeedViewModel.homeFeedLiveData.observe(viewLifecycleOwner, {
            it?.let {
                switchCommonVisibility(isLoading = false)
                setDataToRecyclerView(it)
            }
        })
        podcastPlayerViewModel.changePodcastFullScreenVisibility.observe(viewLifecycleOwner) {
            it?.podcast?.let { podcast ->
                playerBinder.bindToAudioService()
                binding.includeMiniPlayer.clMiniPlayer.setOnClickListener { _ ->
                    openPodcastActivity(it)
                }
                openPodcastActivity(it)
                showMiniPlayerView(podcast)
            }
        }
    }

    private fun setDataToRecyclerView(list: List<FeedItemsQuery.FeedItem>) {
        val adapter = binding.rvHome.adapter
        if (adapter != null) {
            (adapter as HomeFeedAdapter).submitList(list)
        } else {
            setUpRecyclerView(list)
        }
    }

    private fun setUpRecyclerView(list: List<FeedItemsQuery.FeedItem>) {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.layoutManager = layoutManager
        val itemMargin = resources.getDimension(R.dimen.marginMedium).toInt()
        binding.rvHome.addItemDecoration(MarginItemDecoration(itemMargin))
        val adapter = HomeFeedAdapter(podcastPlayerViewModel).apply { submitList(list) }
        rvHome.adapter = adapter
    }

    private fun showMiniPlayerView(podcast: FeedItemsQuery.Podcast) {
        binding.includeMiniPlayer.ivAvatarMiniPlayer.loadCircleImage(
            podcast.images?.small_url ?: ""
        )
        binding.includeMiniPlayer.tvMiniPlayerTitle.text = podcast.title ?: ""

        binding.includeMiniPlayer.btnCloseMiniPlayer.setOnClickListener {
            playerBinder.stopAudioService()
//            changePodcastMiniPlayerVisibility(false)
        }
        changePodcastMiniPlayerVisibility(true)
        binding.includeMiniPlayer.btnMiniPlayerPlay.setOnClickListener {
            playerBinder.playPause(podcast)
        }
    }

    private fun changePodcastMiniPlayerVisibility(visible: Boolean) {
        val materialFade = MaterialFade().apply {
            duration = 100L
        }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)
        binding.includeMiniPlayer.clMiniPlayer.visibility =
            if (visible) View.VISIBLE else View.INVISIBLE
        binding.includeMiniPlayer.clMiniPlayer.isEnabled = visible
    }

    private fun openPodcastActivity(feedItem: FeedItemsQuery.FeedItem) {
        val bundle = PodcastMiniPlayerViewModel.feedItemToBundle(feedItem)
        val intent = Intent(requireContext(), PodcastsActivity::class.java)
        intent.putExtras(bundle)
        val options = ActivityOptions.makeSceneTransitionAnimation(
            requireActivity(),
//            binding.someFooter,
            binding.includeMiniPlayer.clMiniPlayer,
            "podcast_player_transition_label" // The transition name to be matched in Activity B.
        )
        startActivity(intent, options.toBundle())
    }
}