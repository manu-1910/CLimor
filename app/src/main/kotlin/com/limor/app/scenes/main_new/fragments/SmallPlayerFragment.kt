package com.limor.app.scenes.main_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentSmallPlayerBinding
import com.limor.app.extensions.DURATION_READABLE_FORMAT_2
import com.limor.app.extensions.loadCircleImage
import com.limor.app.extensions.toReadableFormat
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import timber.log.Timber
import java.time.Duration
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

    private val playerBinder: PlayerBinder by lazy { (requireActivity() as PlayerViewManager).getPlayerBinder() }

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
            playerBinder.playPause()
        }
        binding.clMiniPlayer.setOnClickListener {
            openExtendedPlayer()
        }
    }

    private fun subscribeToPlayerUpdates() {
        playerBinder.currentPlayPositionLiveData.observe(viewLifecycleOwner) { (seconds, percentage) ->
            binding.cpiPodcastListeningProgress.progress = percentage
            binding.tvMiniplayerSubtitle.text = getString(
                R.string.left, Duration.ofSeconds(seconds).toReadableFormat(
                    DURATION_READABLE_FORMAT_2
                )
            )
        }

        playerBinder.playerStatusLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is PlayerStatus.Cancelled -> Timber.d("Player Cancelled")
                is PlayerStatus.Ended -> binding.btnMiniPlayerPlay.setImageResource(
                    R.drawable.ic_player_play
                )
                is PlayerStatus.Error -> Timber.d("Player Error")
                is PlayerStatus.Other -> Timber.d("Player Other")
                is PlayerStatus.Paused -> binding.btnMiniPlayerPlay.setImageResource(
                    R.drawable.ic_player_play
                )
                is PlayerStatus.Playing -> binding.btnMiniPlayerPlay.setImageResource(
                    R.drawable.pause
                )
            }
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