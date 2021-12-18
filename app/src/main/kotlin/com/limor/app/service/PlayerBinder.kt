package com.limor.app.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.limor.app.common.dispatchers.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.*
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerBinder @Inject constructor(
    private val appContext: Context,
    dispatcherProvider: DispatcherProvider
) {

    private val playerBinderJob = SupervisorJob()
    private val playerBinderScope = CoroutineScope(playerBinderJob + dispatcherProvider.main)

    private var audioService: AudioService? = null
    var currentAudioTrack: AudioService.AudioTrack? = null
        private set
    private var showNotification: Boolean = true

    private val currentPlayingPosition = MutableStateFlow(Duration.ZERO)
    private val playerStatus = MutableStateFlow<PlayerStatus>(PlayerStatus.Init)

    private var previewEndPosition = 0

    private var isPlayingComment = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            audioService = (service as AudioService.AudioServiceBinder).service
            internalPlayPause(currentAudioTrack!!, showNotification)

            audioService?.let { audioService ->
                playerBinderJob.cancelChildren()
                audioService.getPlayerStatus()
                    .onEach {
                        playerStatus.value = it
                        if(it == PlayerStatus.Ended){
                            isPlayingComment = false
                        }
                    }
                    .launchIn(playerBinderScope)

                audioService.getCurrentPlayingPosition()
                    .onEach {
                        currentPlayingPosition.value = Duration.ofMillis(it)
                        if(it > 0 && it >= previewEndPosition && previewEndPosition != 0){
                            stop()
                        }
                    }
                    .launchIn(playerBinderScope)

            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    fun getCurrentTrackDurationInMillis(): Long {
        return audioService?.getDurationInMillis() ?: 0L
    }

    fun pauseIfPlaying(audioTrack: AudioService.AudioTrack) {
        if (audioTrackIsNotPlaying(audioTrack)) {
            return
        }
        pauseCurrentTrack()
    }

    fun audioTrackIsNotPlaying(audioTrack: AudioService.AudioTrack): Boolean {
        return currentAudioTrack != audioTrack || playerStatus.value != PlayerStatus.Playing
    }

    fun audioTrackIsInInitState(audioTrack: AudioService.AudioTrack): Boolean {
        return currentAudioTrack == audioTrack && playerStatus.value == PlayerStatus.Init
    }

    fun getPlayerStatus(audioTrack: AudioService.AudioTrack): Flow<PlayerStatus> {
        return playerStatus.asStateFlow()
            .filter {
                // To make sure that we are listening for the expected track
                currentAudioTrack == audioTrack
            }
    }

    fun getCurrentPlayingPosition(audioTrack: AudioService.AudioTrack): Flow<Duration> {
        return currentPlayingPosition.asStateFlow()
            .filter {
                // To make sure that we are listening for the expected track
                currentAudioTrack == audioTrack
            }
    }

    fun playPause(audioTrack: AudioService.AudioTrack, showNotification: Boolean) {
        currentAudioTrack = audioTrack
        previewEndPosition = 0
        if (audioService == null) {
            bindToAudioService()
        } else {
            internalPlayPause(audioTrack, showNotification)
        }
    }

    fun playPreview(audioTrack: AudioService.AudioTrack, startPosition: Int, endPosition: Int) {
        currentAudioTrack = audioTrack
        previewEndPosition = endPosition
        if (audioService == null) {
            bindToAudioService()
        } else {
            internalPlayPause(audioTrack, showNotification, startPosition)
        }
    }

    fun pauseCurrentTrack() {
        audioService?.pause()
    }

    private fun internalPlayPause(audioTrack: AudioService.AudioTrack, showNotification: Boolean, startPosition: Int = 0, endPosition: Int = 0) {
        audioService?.let { audioService ->
            if (audioService.audioTrack != audioTrack) {
                // Use different track
                audioService.stop()
                audioService.play(
                    audioTrack,
                    withNotification = showNotification,
                    startPosition = startPosition.toLong()
                )
            } else {
                when (playerStatus.value) {
                    is PlayerStatus.Playing -> {
                        if(endPosition == 0){
                            audioService.pause()
                        } else{
                            previewEndPosition = 0
                            audioService.stop()
                        }
                    }
                    is PlayerStatus.Paused -> {
                        audioService.resume()
                    }
                    else -> {
                        audioService.play(
                            audioTrack,
                            withNotification = showNotification,
                            startPosition = startPosition.toLong()
                        )
                    }
                }
            }
        }
    }

    fun stop() {
        previewEndPosition = 0
        isPlayingComment = false
        unbindAudioService()
    }

    fun forward(seekTo: Long) {
        audioService?.forward(seekTo)
    }

    fun rewind(seekTo: Long) {
        audioService?.rewind(seekTo)
    }

    fun seekTo(positionMs: Int) {
        audioService?.seekTo(positionMs)
    }

    private fun bindToAudioService() {
        if (audioService == null) {
            AudioService.newIntent(appContext).also { intent ->
                appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun unbindAudioService() {
        if (audioService != null) {
            audioService?.stop()
            appContext.unbindService(connection)
            audioService = null
            playerStatus.value = PlayerStatus.Init
        }
    }

    fun setIsPlayingComment(boolean: Boolean){
        isPlayingComment = boolean
    }

    fun isPlayingComment() = isPlayingComment

}
