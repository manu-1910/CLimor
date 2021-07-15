package com.limor.app.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.lifecycle.Observer
import com.limor.app.R
import com.limor.app.databinding.ViewCommentAudioPlayerBinding
import com.limor.app.extensions.DURATION_READABLE_FORMAT_1
import com.limor.app.extensions.makeInVisible
import com.limor.app.extensions.makeVisible
import com.limor.app.extensions.toReadableFormat
import com.limor.app.service.PlayerBinder
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.AudioCommentUIModel
import com.limor.app.uimodels.mapToAudioTrack
import timber.log.Timber
import java.time.Duration

class CommentAudioPlayerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_comment_audio_player, this)
    }

    private val binding = ViewCommentAudioPlayerBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var playerBinder: PlayerBinder = PlayerBinder(context.applicationContext)
    private var commentAudioTrack: AudioCommentUIModel? = null

    private val playerProgressObserver = Observer<Pair<Long, Int>> { (seconds, percent) ->
        binding.progressSeekbar.progress = seconds.toInt()
        binding.currentTime.text = Duration.ofSeconds(seconds).toReadableFormat(
            DURATION_READABLE_FORMAT_1
        )
    }

    private val playerStatusObserver = Observer<PlayerStatus?> { status ->
        when (status) {
            is PlayerStatus.Cancelled -> setInitialState(Duration.ZERO)
            is PlayerStatus.Ended -> setPausedState()
            is PlayerStatus.Error -> setErrorState()
            is PlayerStatus.Buffering -> setLoadingState()
            is PlayerStatus.Paused -> setPausedState()
            is PlayerStatus.Playing -> setPlayingState()
            is PlayerStatus.Other -> Timber.d("Other status received: $status")
        }
    }

    init {
        binding.progressSeekbar.apply {
            progress = 0
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
            // FIXME possible state de-sync
            if (binding.playButton.isPaused) {
                playerBinder.playPause()
            } else {
                playerBinder.playPause()
            }
        }
    }

    fun initialize(audio: AudioCommentUIModel) {
        commentAudioTrack = audio
        setInitialState(audio.duration)
        playerBinder.stop()
        playerBinder.start(audio.mapToAudioTrack())
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        playerBinder.playerStatusLiveData.observeForever(playerStatusObserver)
        playerBinder.currentPlayPositionLiveData.observeForever(playerProgressObserver)
    }

    override fun onDetachedFromWindow() {
        playerBinder.playerStatusLiveData.removeObserver(playerStatusObserver)
        playerBinder.currentPlayPositionLiveData.removeObserver(playerProgressObserver)
        playerBinder.stop()
        super.onDetachedFromWindow()
    }
}