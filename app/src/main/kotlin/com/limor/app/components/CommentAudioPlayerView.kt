package com.limor.app.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import com.limor.app.App
import com.limor.app.databinding.ViewCommentAudioPlayerBinding
import com.limor.app.extensions.*
import com.limor.app.service.AudioService
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.AudioCommentUIModel
import com.limor.app.uimodels.mapToAudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration

class CommentAudioPlayerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val binding = ViewCommentAudioPlayerBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var playerBinder: PlayerBinder = App.instance.playerBinder
    private var commentAudioTrack: AudioService.AudioTrack? = null

    init {
        binding.progressSeekbar.apply {
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val progressMs = progress * 1000
                        playerBinder.seekTo(progressMs)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        binding.playButton.setOnClickListener {
            playerBinder.playPause(
                commentAudioTrack!!,
                showNotification = false
            )
        }
    }

    fun initialize(audio: AudioCommentUIModel) {
        commentAudioTrack = audio.mapToAudioTrack()
        setInitialState(audio.duration)

        viewScope.launch(Dispatchers.Main) {
            playerBinder.getPlayerStatus(commentAudioTrack!!)
                .onEach { status ->
                    when (status) {
                        is PlayerStatus.Cancelled -> setInitialState(Duration.ZERO)
                        is PlayerStatus.Ended -> setPausedState()
                        is PlayerStatus.Error -> setErrorState()
                        is PlayerStatus.Buffering -> setLoadingState()
                        is PlayerStatus.Paused -> setPausedState()
                        is PlayerStatus.Playing -> setPlayingState()
                        is PlayerStatus.Other -> Timber.d("Other status received: $status")
                        is PlayerStatus.Init -> setInitialState(Duration.ZERO)
                    }
                }
                .launchIn(this)

            playerBinder.getCurrentPlayingPosition(commentAudioTrack!!)
                .onEach { duration ->
                    binding.progressSeekbar.progress = duration.seconds.toInt()
                    binding.currentTime.text = duration.toReadableFormat(
                        DURATION_READABLE_FORMAT_1
                    )
                }
                .launchIn(this)
        }
    }

    private fun setInitialState(duration: Duration) {
        binding.progressSeekbar.progress = 0
        binding.progressSeekbar.max = duration.seconds.toInt()
        enableSeekbar(false)
        binding.playButton.isPaused = true
        binding.duration.text = duration.toReadableFormat(DURATION_READABLE_FORMAT_1)
        binding.currentTime.text = Duration.ZERO.toReadableFormat(DURATION_READABLE_FORMAT_1)
        binding.loadingBar.makeInVisible()
    }

    private fun setPlayingState() {
        binding.playButton.isPaused = false
        enableSeekbar(true)
        binding.loadingBar.makeInVisible()
    }

    private fun setPausedState() {
        binding.playButton.isPaused = true
        enableSeekbar(true)
        binding.loadingBar.makeInVisible()
    }

    private fun setLoadingState() {
        binding.loadingBar.makeVisible()
        enableSeekbar(false)
    }

    private fun setErrorState() {
        setInitialState(Duration.ZERO)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableSeekbar(enable: Boolean) {
        binding.progressSeekbar.setOnTouchListener { v, event -> !enable }
    }

    override fun onDetachedFromWindow() {
        playerBinder.stop()
        super.onDetachedFromWindow()
    }
}
