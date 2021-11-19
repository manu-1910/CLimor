package com.limor.app.scenes.main_new.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.databinding.FragmentSmallPlayerBinding
import com.limor.app.extensions.*
import com.limor.app.scenes.main.viewmodels.PodcastViewModel
import com.limor.app.scenes.main_new.view_model.ListenPodcastViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import com.limor.app.uimodels.mapToAudioTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class SmallPlayerFragment : BaseFragment() {

    companion object {
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        fun newInstance(castId: Int): SmallPlayerFragment {
            return SmallPlayerFragment().apply {
                arguments = bundleOf(CAST_ID_KEY to castId)
            }
        }
    }

    private var _binding: FragmentSmallPlayerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val podcastViewModel: PodcastViewModel by viewModels { viewModelFactory }
    private val listenPodcastViewModel: ListenPodcastViewModel by viewModels { viewModelFactory }

    private val castId: Int by lazy { requireArguments()[CAST_ID_KEY] as Int }
    private var restarted = false

    private var playerUpdatesJob: Job? = null

    @Inject
    lateinit var playerBinder: PlayerBinder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmallPlayerBinding.inflate(inflater, container, false)
        podcastViewModel.loadCast(castId)
        subscribeToCastUpdates()
        setClickListeners()
        return binding.root
    }

    private fun subscribeToCastUpdates() {
        podcastViewModel.cast.observe(viewLifecycleOwner) { cast ->
            initPlayerViews(cast)
            subscribeToPlayerUpdates(cast)
        }
    }

    private fun setClickListeners(){
        binding.btnCloseMiniPlayer.setOnClickListener {
            closePlayer()
        }
    }

    private fun initPlayerViews(cast: CastUIModel) {
        cast.imageLinks?.large?.let {
            binding.ivAvatarMiniPlayer.loadCircleImage(it)
        }
        binding.tvMiniPlayerTitle.text = cast.title

        binding.btnMiniPlayerPlay.setOnClickListener {
            Timber.d("LOGGGG")
            cast.audio?.let { audio ->
                if(playerBinder.audioTrackIsInInitState(audio.mapToAudioTrack())){
                    restarted = true
                }
                playerBinder.playPause(audio.mapToAudioTrack(), showNotification = true)
            }
        }
        binding.ivAvatarMiniPlayer.setOnClickListener {
            Timber.d("LOGGGG")
            cast.audio?.let { audio ->
                playerBinder.playPause(audio.mapToAudioTrack(), showNotification = true)
            }
        }
        binding.clMiniPlayer.setOnClickListener {
            Timber.d("Layout click")
            openExtendedPlayer()
        }
        if(cast.imageLinks?.large == null){
            binding.circleImageView2.setColorFilter(Color.parseColor(cast.colorCode))
        }

    }

    private fun subscribeToPlayerUpdates(cast: CastUIModel) {
        playerUpdatesJob?.cancel()
        playerUpdatesJob = lifecycleScope.launchWhenCreated {
            val audioModel = cast.audio!!.mapToAudioTrack()
            playerBinder.getCurrentPlayingPosition(audioModel)
                .onEach { duration ->
                    if(audioModel.duration.seconds>0){
                        binding.cpiPodcastListeningProgress.progress =
                            ((duration.seconds * 100) / audioModel.duration.seconds).toInt()
                        binding.tvMiniplayerSubtitle.text = getString(
                            R.string.progress, duration.toReadableStringFormat(DURATION_READABLE_FORMAT_3)
                        )

                    }
                }
                .launchIn(this)

            playerBinder.getPlayerStatus(audioModel)
                .onEach {
                    when (it) {
                        is PlayerStatus.Cancelled -> Timber.d("Player Cancelled")
                        is PlayerStatus.Ended -> {
                            showLoading(false)
                            binding.btnMiniPlayerPlay.setImageResource(
                                R.drawable.ic_player_play
                            )
                            listenPodcastViewModel.listenPodcast(castId)
                            restarted = false
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
                castId = castId,
                playerType = PlayerViewManager.PlayerType.EXTENDED,
                maximizedFromMiniPlayer = true,
                restarted = restarted
            )
        )
    }

    private fun closePlayer() {
        if(restarted){
            listenPodcastViewModel.listenPodcast(castId)
        }
        (activity as? PlayerViewManager)?.hidePlayer()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
