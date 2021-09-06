package com.limor.app.scenes.utils.voicebio

import android.os.Handler
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.limor.app.App
import com.limor.app.BuildConfig
import com.limor.app.scenes.utils.voicePlayer.LimorMediaPlayer
import com.limor.app.service.AudioService
import com.limor.app.service.PlayerStatus
import com.limor.app.service.recording.CompressedAudioRecorder
import com.limor.app.service.recording.RecorderCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.time.Duration
import kotlin.math.min

class VoiceBioPresenter(val viewModel: VoiceBioContract.ViewModel) : VoiceBioContract.Presenter,
    RecorderCallback {

    val uiState = ObservableField(VoiceBioUiState.CanRecord)
    val playerState = ObservableField(VoiceBioPlayerState.Idle)
    val audioReadablePosition = ObservableField(zeroPosition)
    val audioPositionPercentage = ObservableField(1f)

    private var audioTrack: AudioService.AudioTrack? = null
    private var currentDuration = 0L
    private var path: String? = null
    private var playerBinder = App.instance.playerBinder
    private var binderJob: Job? = null
    private val progressHandler by lazy {
        Handler()
    }

    private val progressUpdate: Runnable = object : Runnable {
        override fun run() {
            mediaPlayer.let {
                if (it.isPlaying) {
                    audioPositionPercentage.set(min(1f, it.currentPosition.toFloat() / currentDuration.toFloat()))
                    setPositionLabel(it.currentPosition.toLong())
                    updatePlayPositionLabel()
                }
            }
        }
    }

    init {
        CompressedAudioRecorder.callback = this

        viewModel.getAudioURL()?.let {
            uiState.set(VoiceBioUiState.CanRecordAndPlay)
            playerState.set(VoiceBioPlayerState.Idle)
            playWithSource(it)
        }
    }

    private fun updatePlayPositionLabel() {
        progressHandler.postDelayed(progressUpdate, updateInterval)
    }

    private val mediaPlayer: LimorMediaPlayer by lazy {
        LimorMediaPlayer().apply {
            setOnCompletionListener { onPlayComplete() }
            setOnErrorListener { _, _, _ -> true }
        }
    }

    private fun onPlayComplete() {
        println("On play complete...")
        audioReadablePosition.set(zeroPosition)
        playerState.set(VoiceBioPlayerState.Idle)
        audioPositionPercentage.set(0f)
    }

    override fun startRecording() {
        if (!viewModel.ensurePermissions()) {
            return
        }

        // Before starting a new recording we make sure that any playback (local or not) is
        // paused and/or stopped
        pauseAudioPlaying()
        disableRemoteTrack()

        // Reset the UI state
        currentDuration = 0
        uiState.set(VoiceBioUiState.CanRecordAndPlay)
        playerState.set(VoiceBioPlayerState.Recording)
        viewModel.resetVisualization()

        // Get a new file path to record into and start recording
        viewModel.getNextAudioFilePath().let {
            path = it
            CompressedAudioRecorder.startRecording(it, viewModel.getContext())
        }
    }

    private fun disableRemoteTrack() {
        audioTrack?.let {
            App.instance.playerBinder.pauseIfPlaying(it)
        }
        audioTrack = null

        binderJob?.cancel()
        binderJob = null
    }

    private fun stopRecordingProcess() {
        println("Stopping the recording process...")
        viewModel.resetVisualization()
        CompressedAudioRecorder.stopRecording(viewModel.getContext())
    }

    override fun stopRecording() {
        audioReadablePosition.set(zeroPosition)

        uiState.set(VoiceBioUiState.CanRecordAndPlay)
        playerState.set(VoiceBioPlayerState.Idle)

        stopRecordingProcess()
        viewModel.setAudioInfo(path, currentDuration / 1000.0)
    }

    override fun playStopRecord() {
        playerState.set(if (playerState.get() == VoiceBioPlayerState.Idle) VoiceBioPlayerState.Playing else VoiceBioPlayerState.Idle)
        togglePlayStop()
    }

    override fun deleteRecord() {
        pauseAudioPlaying()
        stopRecordingProcess()
        deleteLastRecording()
        uiState.set(VoiceBioUiState.CanRecord)
        audioReadablePosition.set(zeroPosition)
        viewModel.setAudioInfo()
    }

    override fun setAudioURL(url: String?) {
        if (url.isNullOrEmpty()) {
            disableRemoteTrack()
            return
        }
        val voiceBioUrl = url ?: return
        val at = AudioService.AudioTrack(
            url = voiceBioUrl,
            duration = Duration.ZERO,
            title = null
        )
        audioTrack = at

        playerState.set(VoiceBioPlayerState.Idle)
        uiState.set(VoiceBioUiState.CanRecordAndPlay)

        listenToBinderEvents(at)
    }

    private fun listenToBinderEvents(audioTrack: AudioService.AudioTrack) {
        val viewScope = viewModel.getScope()
        binderJob = viewScope.launch(Dispatchers.Main) {
            playerBinder.getPlayerStatus(audioTrack)
                .onEach { status ->
                    when (status) {
                        is PlayerStatus.Cancelled -> {
                            // TODO?
                        }
                        is PlayerStatus.Ended -> {
                            onPlayComplete()
                        }
                        is PlayerStatus.Error -> {
                            // TODO?
                        }
                        is PlayerStatus.Buffering -> {
                            // TODO?
                        }
                        is PlayerStatus.Paused -> {
                            // TODO?
                        }
                        is PlayerStatus.Playing -> {
                            // TODO?
                        }
                        is PlayerStatus.Other -> {
                            Timber.d("Other status received: $status")
                        }
                        is PlayerStatus.Init -> {
                            // TODO?
                        }
                    }
                }
                .launchIn(this)

            playerBinder.getCurrentPlayingPosition(audioTrack)
                .onEach { duration ->
                    println("Duration -> ${duration.toMillis()} of ${playerBinder.getCurrentTrackDurationInMillis()}")
                    val pos = duration.toMillis()
                    setPositionLabel(pos)
                    audioPositionPercentage.set(pos.toFloat() / playerBinder.getCurrentTrackDurationInMillis().toFloat())
                }
                .launchIn(this)

        }
    }

    private fun deleteLastRecording() {
        path?.let { filePath ->
            File(filePath).delete().also {
                if (BuildConfig.DEBUG) {
                    println("Deleted $filePath successfully -> $it")
                }
            }
        }
        path = null
    }

    private fun pauseAudioPlaying() {
        App.instance.playerBinder.pauseCurrentTrack()
        audioTrack?.let { App.instance.playerBinder.pauseIfPlaying(it) }

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    /**
     * This internally is always called as a consequence to an UI action
     * (i.e. pressing the play/pause button)
     */
    private fun togglePlayStop() {
        when(playerState.get()) {
            VoiceBioPlayerState.Idle -> {
                // this is Playing -> Idle
                pauseAudioPlaying()
            }
            VoiceBioPlayerState.Playing -> {
                // this is Idle -> Playing (could also be resume)
                playAudio()
            }
            else -> {
                // TODO throw illegal state? shouldn't ever happen for the VoiceBioView but other
                // views might not handle states properly, so maybe throw?
            }
        }
    }

    companion object {
        @BindingAdapter("app:audioPosition")
        @JvmStatic
        fun audioPosition(view: View, audioPositionPercentage: Float) {
            view.x = (audioPositionPercentage - 1) * view.width
        }

        // 60 fps = 16.667 ms per frame, but 15 fps is good enough for this
        private const val updateInterval = (16.667 * 4).toLong()
        private const val zeroPosition = "0:00"
        private const val maxBioDurationMillis = 90 * 1000
    }

    override fun onStartRecord() {
        // nothing
    }

    override fun onPauseRecord() {
        // not handled here
    }

    override fun onResumeRecord() {
        // not handled here
    }

    private fun setPositionLabel(millis: Long) {
        val actualSeconds = millis / 1000
        val minutes = actualSeconds.div(60)
        val seconds = actualSeconds % 60
        audioReadablePosition.set("$minutes:${seconds.toString().padStart(2, '0')}")
    }

    override fun onRecordProgress(millis: Long, amp: Int) {
        // Long -> Int is safe because the tick time is very small, around 16 ms

        val delta = millis - currentDuration
        currentDuration = millis
        viewModel.addAmp(amp, delta.toInt())

        val remainingMillis = maxOf(0,  maxBioDurationMillis - millis)
        if (remainingMillis == 0L) {
            stopRecording()
        }

        setPositionLabel(remainingMillis)
    }

    override fun onStopRecord() {
        // not handled here
    }

    override fun onError(throwable: Throwable?) {
        // TODO ?
    }

    private fun startPlayback() {
        mediaPlayer.start()
        updatePlayPositionLabel()
    }

    private fun playAudio() {
        if (audioPositionPercentage.get() == 1f) {
            audioPositionPercentage.set(2f)
        }
        audioTrack?.let {
            App.instance.playerBinder.playPause(it, false)
            return
        }
        playWithFile()
    }

    private fun playWithFile() {
        playWithSource(path)
    }

    private fun playWithSource(source: String?) {
        val path = source ?: return

        if (mediaPlayer.lastDataSource == path) {
            // this is resuming
            startPlayback()
            return
        }

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()

        mediaPlayer.setDataSource(path)

        try {
            mediaPlayer.prepare()
            startPlayback()

        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}