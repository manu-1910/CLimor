package com.limor.app.scenes.main_new.fragments

import android.graphics.Color
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

class SmallPlayerFragment : BaseFragment(), PlayerFragment {

    companion object {
        private const val CAST_ID_KEY = "CAST_ID_KEY"
        private const val KEY_CAST_IDS = "KEY_CAST_IDS"

        fun newInstance(castId: Int, castIds: List<Int>?): SmallPlayerFragment {
            return SmallPlayerFragment().apply {
                arguments = bundleOf(
                    CAST_ID_KEY to castId,
                    KEY_CAST_IDS to castIds?.toIntArray()
                )
            }
        }
    }

    private var _binding: FragmentSmallPlayerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val podcastViewModel: PodcastViewModel by viewModels { viewModelFactory }
    private val listenPodcastViewModel: ListenPodcastViewModel by viewModels { viewModelFactory }

    private var castId: Int = 0
    private val castIds: List<Int>? by lazy {
        (requireArguments()[KEY_CAST_IDS] as? IntArray)?.asList()
    }
    private val isInPlaylist by lazy {
        (castIds?.size ?: 0) > 1
    }

    private var restarted = false

    private var playerUpdatesJob: Job? = null

    private var currentCast: CastUIModel? = null

    @Inject
    lateinit var playerBinder: PlayerBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        castId = requireArguments()[CAST_ID_KEY] as Int
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSmallPlayerBinding.inflate(inflater, container, false)
        podcastViewModel.loadCast(castId)
        subscribeToCastUpdates()
        setClickListeners()
        return binding.root
    }

    private fun subscribeToCastUpdates() {
        podcastViewModel.cast.observe(viewLifecycleOwner) { cast ->
            currentCast = cast
            initPlayerViews(cast)
            subscribeToPlayerUpdates(cast)
            playPlaylistPodcast()
        }
    }

    private fun playPlaylistPodcast() {
        if (!isInPlaylist) {
            return
        }
        val cast = currentCast ?: return
        val audio = cast.audio ?: return
        val track = audio.mapToAudioTrack()
        if (playerBinder.audioTrackIsNotPlaying(track)) {
            playerBinder.playPause(track, showNotification = true)
        }
    }

    private fun setClickListeners() {
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
                if (playerBinder.audioTrackIsInInitState(audio.mapToAudioTrack())) {
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
        if (cast.imageLinks?.large == null) {
            cast.colorCode?.let{
                binding.circleImageView2.setColorFilter(Color.parseColor(it))
            }

        }

    }

    private fun playNextPodcast() {
        if (!isInPlaylist) {
            return
        }

        onPlaylistNavigation(1)
    }

    private fun onPlaylistNavigation(direction: Int) {
        if (!isInPlaylist) {
            return
        }
        val ids = castIds ?: return
        val index = ids.indexOf(castId)
        val next = index + direction

        if (next < 0 || next == ids.size) {
            return
        }

        castId = ids[next]
        podcastViewModel.loadCast(castId)
    }

    private fun subscribeToPlayerUpdates(cast: CastUIModel) {
        playerUpdatesJob?.cancel()
        playerUpdatesJob = lifecycleScope.launchWhenCreated {
            val audioModel = cast.audio!!.mapToAudioTrack()
            playerBinder.getCurrentPlayingPosition(audioModel)
                .onEach { duration ->
                    if (audioModel.duration.seconds > 0) {
                        binding.cpiPodcastListeningProgress.progress =
                            ((duration.seconds * 100) / audioModel.duration.seconds).toInt()
                        binding.tvMiniplayerSubtitle.text = getString(
                            R.string.progress,
                            duration.toReadableStringFormat(DURATION_READABLE_FORMAT_3)
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

                            playNextPodcast()
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
                castIds = castIds,
                playerType = PlayerViewManager.PlayerType.EXTENDED,
                maximizedFromMiniPlayer = true,
                restarted = restarted
            )
        )
    }

    private fun closePlayer() {
        if (restarted) {
            listenPodcastViewModel.listenPodcast(castId)
        }
        (activity as? PlayerViewManager)?.hidePlayer()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun getCastId(): Int {
        return castId
    }
}
