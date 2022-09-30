package com.limor.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.limor.app.App
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.service.recording.CompressedAudioRecorder
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.anko.runOnUiThread
import java.time.Duration
import java.util.*

private const val PLAYBACK_CHANNEL_ID = "com.limor.app.playback_channel"
private const val MEDIA_SESSION_TAG = "com.limor.app.audio"
private const val PLAYBACK_NOTIFICATION_ID = 1
private const val PLAYBACK_TIMER_DELAY = 64L
private const val PLAYBACK_SKIP_INCREMENTS = 30000L

class AudioService : Service() {

    inner class AudioServiceBinder : Binder() {
        val service
            get() = this@AudioService
    }

    companion object {
        @MainThread
        fun newIntent(
            context: Context
        ) = Intent(context, AudioService::class.java)
    }

    private lateinit var exoPlayer: SimpleExoPlayer
    private var playbackTimer: Timer? = null

    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null
    var audioTrack: AudioTrack? = null
        private set

    private var isInitialized = false

    /**
     * Current playing position in milliseconds
     */
    private val currentPlayingPosition = MutableStateFlow(0L)
    private val playerStatus = MutableStateFlow<PlayerStatus>(PlayerStatus.Init)

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    private fun initialize() {
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .build()
        exoPlayer.addListener(PlayerEventListener())
        isInitialized = true
    }

    override fun onBind(intent: Intent?): IBinder {
        return AudioServiceBinder()
    }

    fun getPlayerStatus() = playerStatus.asStateFlow()
    fun getCurrentPlayingPosition() = currentPlayingPosition.asStateFlow()

    @MainThread
    fun play(audioTrack: AudioTrack, startPosition: Long = 0, withNotification: Boolean = true) {
        if (!isInitialized) {
            initialize()
        }

        this.audioTrack = audioTrack

        val userAgent = Util.getUserAgent(applicationContext, BuildConfig.APPLICATION_ID)

        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(applicationContext, userAgent)
        ).createMediaSource(MediaItem.fromUri(audioTrack.url))

        if (withNotification) {
            showNotification()
        }

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()

        val haveStartPosition = startPosition != C.POSITION_UNSET.toLong()
        if (haveStartPosition) {
            exoPlayer.seekTo(startPosition)
        }

        resume()
    }

    @MainThread
    fun resume() {
        CompressedAudioRecorder.stopRecording(this)
        App.instance.interruptAllMediaPlayers()
        exoPlayer.playWhenReady = true
    }

    @MainThread
    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun seekTo(positionMs: Int) {
        exoPlayer.seekTo(positionMs.toLong())
    }

    fun getDurationInMillis(): Long {
        return exoPlayer.duration
    }

    fun setPlayerReady(){
        exoPlayer.playWhenReady = true
    }

    fun forward(skipLength: Long = PLAYBACK_SKIP_INCREMENTS) {
        if (exoPlayer.currentPosition + skipLength < exoPlayer.duration) {
            exoPlayer.seekTo(exoPlayer.currentPosition + skipLength)
        } else {
            exoPlayer.seekTo(exoPlayer.duration)
        }
    }

    fun rewind(skipLength: Long = PLAYBACK_SKIP_INCREMENTS) {
        if (exoPlayer.currentPosition - skipLength > 0) {
            exoPlayer.seekTo(exoPlayer.currentPosition - skipLength)
        } else {
            exoPlayer.seekTo(0)
        }
    }

    @MainThread
    fun stop() {
        cancelPlaybackMonitor()


        currentPlayingPosition.value = 0L
        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null)
        playerNotificationManager?.setPlayer(null)
        exoPlayer.release()

        isInitialized = false
    }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    @MainThread
    private fun monitorPlaybackProgress() {
        if (playbackTimer == null) {
            playbackTimer = Timer()

            playbackTimer?.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            currentPlayingPosition.value = exoPlayer.currentPosition
                            if (exoPlayer.duration - exoPlayer.contentPosition <= PLAYBACK_TIMER_DELAY) {
                                playbackTimer?.cancel()
                            }
                        }
                    }
                },
                PLAYBACK_TIMER_DELAY,
                PLAYBACK_TIMER_DELAY
            )
        }
    }

    @MainThread
    fun cancelPlaybackMonitor() {
        playbackTimer?.cancel()
        playbackTimer = null
    }

    private inner class PlayerEventListener : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    playerStatus.value = PlayerStatus.Init
                }
                Player.STATE_BUFFERING -> {
                    playerStatus.value = PlayerStatus.Buffering
                }
                Player.STATE_READY -> {
                    if (playWhenReady) {
                        playerStatus.value = PlayerStatus.Playing
                    } else {
                        playerStatus.value = PlayerStatus.Paused
                    }
                }
                Player.STATE_ENDED -> {
                    playerStatus.value = PlayerStatus.Ended
                    Handler().postDelayed(Runnable {
                        playerStatus.value = PlayerStatus.Init
                    }, 1000)
                }
                else -> {
                    playerStatus.value = PlayerStatus.Other
                }
            }

            // Only monitor playback to record progress when playing.
            if (playbackState == Player.STATE_READY && exoPlayer.playWhenReady) {
                monitorPlaybackProgress()
            } else {
                cancelPlaybackMonitor()
            }
        }

        override fun onPlayerError(e: ExoPlaybackException) {
            playerStatus.value = PlayerStatus.Error(e)
        }
    }

    private fun showNotification() {
        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            PLAYBACK_NOTIFICATION_ID,
            PLAYBACK_CHANNEL_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): String {
                    return audioTrack?.title ?: "..."
                }

                @Nullable
                override fun createCurrentContentIntent(player: Player): PendingIntent {
                    // TODO use special intent argument to open player view inside activity
                    var flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                        flags = flags or PendingIntent.FLAG_IMMUTABLE
                    }
                    return PendingIntent.getActivity(
                        applicationContext,
                        0,
                        Intent(applicationContext, MainActivityNew::class.java),
                        flags
                    )
                }

                @Nullable
                override fun getCurrentContentText(player: Player): String? {
                    return null
                }

                @Nullable
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return ContextCompat.getDrawable(applicationContext, R.drawable.logotype)
                        ?.toBitmap()
                }
            }
        ).setNotificationListener(
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    playerStatus.value = PlayerStatus.Cancelled
                    stop()
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing) {
                        // Make sure the service will not get destroyed while playing media.
                        startForeground(notificationId, notification)
                    } else {
                        // Make notification cancellable.
                        stopForeground(false)
                    }
                }
            }
        )
            .setChannelNameResourceId(R.string.app_name)
            .build().apply {
                setUseNextAction(false)
                setUsePreviousAction(false)
                setUseStopAction(true)
                setControlDispatcher(
                    DefaultControlDispatcher(
                        PLAYBACK_SKIP_INCREMENTS,
                        PLAYBACK_SKIP_INCREMENTS
                    )
                )
                setPlayer(exoPlayer)
            }

        // Show lock screen controls and let apps like Google assistant manager playback.
        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG).apply {
            isActive = true
        }.also { mediaSession ->
            playerNotificationManager?.setMediaSessionToken(mediaSession.sessionToken)

            mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
                setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                    override fun getMediaDescription(
                        player: Player,
                        windowIndex: Int
                    ): MediaDescriptionCompat {
                        val bitmap =
                            ContextCompat.getDrawable(applicationContext, R.drawable.logotype)
                                ?.toBitmap()
                        val extras = Bundle().apply {
                            putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                            putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
                        }

                        val title = audioTrack?.title ?: "..."

                        return MediaDescriptionCompat.Builder()
                            .setIconBitmap(bitmap)
                            .setTitle(title)
                            .setExtras(extras)
                            .build()
                    }
                })

                setPlayer(exoPlayer)
            }
        }
    }

    @Parcelize
    data class AudioTrack(
        val url: String,
        val title: String?,
        val duration: Duration,
        var startPlayingFrom: Long = -1
    ) : Parcelable{
        override fun equals(other: Any?) = Audio(this) == Audio(other as AudioTrack)
    }

    data class Audio(val url: String){
        constructor(track: AudioTrack) : this(track.url)
    }

}
