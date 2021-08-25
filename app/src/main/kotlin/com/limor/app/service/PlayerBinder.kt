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

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            audioService = (service as AudioService.AudioServiceBinder).service
            internalPlayPause(currentAudioTrack!!, showNotification)

            audioService?.let { audioService ->
                playerBinderJob.cancelChildren()
                audioService.getPlayerStatus()
                    .onEach {
                        playerStatus.value = it
                    }
                    .launchIn(playerBinderScope)

                audioService.getCurrentPlayingPosition()
                    .onEach {
                        currentPlayingPosition.value = Duration.ofMillis(it)
                    }
                    .launchIn(playerBinderScope)

            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
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
        if (audioService == null) {
            bindToAudioService()
        } else {
            internalPlayPause(audioTrack, showNotification)
        }
    }

    fun pauseCurrentTrack() {
        audioService?.pause()
    }

    private fun internalPlayPause(audioTrack: AudioService.AudioTrack, showNotification: Boolean) {
        audioService?.let { audioService ->
            if (audioService.audioTrack != audioTrack) {
                // Use different track
                audioService.stop()
                audioService.play(
                    audioTrack,
                    withNotification = showNotification
                )
            } else {
                when (playerStatus.value) {
                    is PlayerStatus.Playing -> {
                        audioService.pause()
                    }
                    is PlayerStatus.Paused -> {
                        audioService.resume()
                    }
                    else -> {
                        audioService.play(
                            audioTrack,
                            withNotification = showNotification
                        )
                    }
                }
            }
        }
    }

    fun stop() {
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
}
