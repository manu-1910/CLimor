package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentSmallPlayerBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToAudioTrack
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class SmallPlayerFragment : BaseFragment() {

    companion object {
        private const val CAST_KEY = "CAST_KEY"
        fun newInstance(cast: CastUIModel): SmallPlayerFragment {
            return SmallPlayerFragment().apply {
                arguments = bundleOf(CAST_KEY to cast)
            }
        }
    }

    private var _binding: FragmentSmallPlayerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val podcast: CastUIModel by lazy { requireArguments()[CAST_KEY] as CastUIModel }
    private val podcastAudio get() = podcast.audio!!.mapToAudioTrack()

    @Inject
    lateinit var playerBinder: PlayerBinder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmallPlayerBinding.inflate(inflater, container, false)
        initPlayerViews()
        subscribeToPlayerUpdates()
        return binding.root
    }

    private fun initPlayerViews() {
        podcast.imageLinks?.small?.let {
            binding.ivAvatarMiniPlayer.loadCircleImage(it)
        }
        binding.tvMiniPlayerTitle.text = podcast.title

        binding.btnCloseMiniPlayer.setOnClickListener {
            closePlayer()
        }
        binding.ivAvatarMiniPlayer.setOnClickListener {
            podcast.audio?.let { audio ->
                playerBinder.playPause(audio.mapToAudioTrack(), showNotification = true)
            }
        }
        binding.clMiniPlayer.setOnClickListener {
            openExtendedPlayer()
        }
    }

    private fun subscribeToPlayerUpdates() {
        lifecycleScope.launchWhenCreated {
            playerBinder.getCurrentPlayingPosition(podcastAudio)
                .onEach { duration ->
                    binding.cpiPodcastListeningProgress.progress =
                        ((duration.seconds * 100) / podcast.audio?.duration?.seconds!!).toInt()
                    binding.tvMiniplayerSubtitle.text = getString(
                        R.string.left, duration.toReadableFormat(DURATION_READABLE_FORMAT_2)
                    )
                }
                .launchIn(this)

            playerBinder.getPlayerStatus(podcastAudio)
                .onEach {
                    when (it) {
                        is PlayerStatus.Cancelled -> Timber.d("Player Cancelled")
                        is PlayerStatus.Ended -> {
                            showLoading(false)
                            binding.btnMiniPlayerPlay.setImageResource(
                                R.drawable.ic_player_play
                            )
                        }
                        is PlayerStatus.Error -> {
                            showLoading(false)
                            Timber.d("Player Error")
                        }
                        is PlayerStatus.Paused -> {
                            showLoading(false)
                            binding.btnMiniPlayerPlay.setImageResource(
                                R.drawable.ic_player_play
                            )
                        }
                        is PlayerStatus.Playing -> {
                            showLoading(false)
                            binding.btnMiniPlayerPlay.setImageResource(
                                R.drawable.pause
                            )
                        }
                        is PlayerStatus.Init -> Timber.d("Player Init")
                        is PlayerStatus.Other -> Timber.d("Player Other")
                        is PlayerStatus.Buffering -> {
                            showLoading(true)
                        }
                    }
                }
                .launchIn(this)
        }
    }

    private fun showLoading(show: Boolean) {
        if (binding.cpiPodcastListeningProgress.isIndeterminate != show) {
            binding.cpiPodcastListeningProgress.makeGone()
            binding.cpiPodcastListeningProgress.isIndeterminate = show
            binding.cpiPodcastListeningProgress.makeVisible()
        }
    }

    private fun openExtendedPlayer() {
        (activity as? PlayerViewManager)?.showPlayer(
            PlayerViewManager.PlayerArgs(
                cast = podcast,
                playerType = PlayerViewManager.PlayerType.EXTENDED
            )
        )
    }

    private fun closePlayer() {
        (activity as? PlayerViewManager)?.hidePlayer()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}