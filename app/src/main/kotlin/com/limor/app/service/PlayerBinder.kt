package com.limor.app.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.limor.app.uimodels.CastUIModel
import timber.log.Timber

class PlayerBinder(
    val appContext: Context,
) : LifecycleObserver {

    private var audioService: AudioService? = null
    private var playerStatus: PlayerStatus? = null
    private var currentPodcast: CastUIModel? = null

    private val _playerStatusLiveData = MutableLiveData<PlayerStatus?>()
    val playerStatusLiveData: LiveData<PlayerStatus?>
        get() = _playerStatusLiveData

    // <POSITION to PERCENT>
    private val _currentPlayPositionLiveData = MutableLiveData<Pair<Long, Int>>()
    val currentPlayPositionLiveData: LiveData<Pair<Long, Int>>
        get() = _currentPlayPositionLiveData


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service
            audioService?.playerStatusLiveData?.observeForever {
                Timber.d("PlayerStatus $it")
                playerStatus = it
                _playerStatusLiveData.postValue(playerStatus)
            }

            audioService?.currentPlayingPosition?.distinctUntilChanged()
                ?.observeForever { playingPositionMillis ->

                    val percent = getProgressPercent(
                        playingPositionMillis,
                        currentPodcast?.audio?.duration?.toMillis() ?: 0
                    )
                    val playingPositionSeconds = playingPositionMillis / 1000
                    _currentPlayPositionLiveData.postValue(playingPositionSeconds to percent)
                }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    fun start(podcast: CastUIModel) {
        currentPodcast = podcast

        if (audioService == null) {
            bindToAudioService()
        }
    }

    fun stop() {
        unbindAudioService()
    }

    fun playPause() {
        if (audioService == null) {
            Timber.e("Trying to play/pause when service is not bound")
            return
        }

        when (playerStatus) {
            is PlayerStatus.Playing -> {
                audioService?.pause()
            }
            is PlayerStatus.Paused -> {
                audioService?.resume()
            }
            else -> {
                audioService?.play(
                    currentPodcast,
                    1L,
                    1F
                )
            }
        }
    }

    fun forward(seekTo: Long) {
        audioService?.forward(seekTo)
    }

    fun rewind(seekTo: Long) {
        audioService?.rewind(seekTo)
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
            audioService?.pause()
            appContext.unbindService(connection)
            audioService = null
            playerStatus = null
        }
    }

    companion object {
        fun getProgressPercent(currentPosition: Long?, duration: Long?): Int {
            if (currentPosition == null || duration == null || duration == 0L) return 0
            return (currentPosition * 100 / duration).toInt()
        }
    }
}
