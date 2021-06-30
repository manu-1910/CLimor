package com.limor.app.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.*
import com.limor.app.FeedItemsQuery
import com.limor.app.scenes.main_new.utils.PodcastMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference

class PlayerBinder(
    val lifecycleOwner: LifecycleOwner,
    val contextReference: WeakReference<Context>,
) : LifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    private var audioService: AudioService? = null
    private var playerStatus: PlayerStatus? = null
    private var lastPlayingPosition = 0L
    private var lastPodcastId: Int = -1

    private val _playerStatusLiveData = MutableLiveData<PlayerStatus?>()
    val playerStatusLiveData: LiveData<PlayerStatus?>
        get() = _playerStatusLiveData

    // <POSITION to PERCENT>
    private val _currentPlayPositionLiveData = MutableLiveData<Pair<Long?, Int>>()
    val currentPlayPositionLiveData: LiveData<Pair<Long?, Int>>
        get() = _currentPlayPositionLiveData


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service
            audioService?.playerStatusLiveData?.observe(lifecycleOwner) {
                Timber.d("PlayerStatus $it")
                playerStatus = it
                _playerStatusLiveData.postValue(playerStatus)
            }

            audioService?.currentPlayingPosition?.observe(
                lifecycleOwner,
                { playingPosition ->
                    lastPlayingPosition = playingPosition
                    val percent = getProgressPercent(
                        lastPlayingPosition,
                        audioService?.uiPodcast?.audio?.duration ?: 0
                    )
                    _currentPlayPositionLiveData.postValue(lastPlayingPosition to percent)

                })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        Timber.d("PlayerImpl onStart")
        bindToAudioService()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Timber.d("PlayerImpl onStop")
        unbindAudioService()
    }

    fun startPlayPodcast(scope: CoroutineScope, podcast: FeedItemsQuery.Podcast) {
        scope.launch {
            while (audioService == null){
                bindToAudioService()
                delay(200)
            }

            //same podcast case
            if(podcast.id == audioService?.uiPodcast?.id){
                if(playerStatus !is PlayerStatus.Playing)
                    playPause(podcast)
                return@launch
            }

            val uiPodcast =
                try {
                    PodcastMapper.podcastToUIPodcast(podcast)
                } catch (e: Exception) {
                    Timber.e(e)
                    return@launch
                }
            AudioService.newIntent(contextReference.get()!!, uiPodcast, 1L)
                .also { intent ->
                    contextReference.get()!!.startService(intent)
                }
        }
    }

    fun playPause(podcast: FeedItemsQuery.Podcast) {
        if (audioService == null || podcast.id == null) return

        when (playerStatus) {
            is PlayerStatus.Playing -> {
                audioService?.pause()

            }
            is PlayerStatus.Paused -> {
                audioService?.resume()
            }
            is PlayerStatus.Ended -> {
                audioService?.play(
                    audioService?.uiPodcast?.audio?.audio_url,
                    1L,
                    1F
                )
            }
        }

//        val positionToStart =
//            if (podcast.id != lastPodcastId) {
//                lastPodcastId = podcast.id
//                0
//            } else
//                lastPlayingPosition
//
//        audioService?.play(podcast.audio?.audio_url, positionToStart)
    }

    fun bindToAudioService() {
        if (audioService == null && contextReference.get() != null) {
            AudioService.newIntent(contextReference.get()!!).also { intent ->
                contextReference.get()!!.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }


    fun stopAudioService() {
        audioService?.pause()

        unbindAudioService()
        contextReference.get()
            ?.stopService(Intent(contextReference.get(), AudioService::class.java))

        audioService = null
        playerStatus = null
    }

    private fun unbindAudioService() {
        if (audioService != null) {
            contextReference.get()?.unbindService(connection)
            audioService = null
        }
    }

    fun forward(seekTo: Long){
        audioService?.forward(seekTo)
    }

    fun rewind(seekTo: Long){
        audioService?.rewind(seekTo)
    }

    companion object {
        fun getProgressPercent(currentPosition: Long?, duration: Int?): Int {
            if (currentPosition == null || duration == null || duration == 0) return 0
            return (currentPosition * 100 / duration).toInt()
        }
    }
}