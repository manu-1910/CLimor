package io.square1.limor.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.square1.limor.BuildConfig
import io.square1.limor.R
import io.square1.limor.scenes.main.MainActivity
import io.square1.limor.uimodels.UIPodcast
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.util.*

private const val PLAYBACK_CHANNEL_ID = "io.square1.limor.playback_channel"
private const val MEDIA_SESSION_TAG = "io.square1.limor.audio"
private const val PLAYBACK_NOTIFICATION_ID = 1
private const val PLAYBACK_TIMER_DELAY = 1000L
private const val ARG_PODCAST = "ARG_PODCAST"
private const val ARG_START_POSITION = "ARG_START_POSITION"

class AudioService : Service() {

    inner class AudioServiceBinder : Binder() {
        val service
            get() = this@AudioService

        val exoPlayer
            get() = this@AudioService.exoPlayer
    }

    companion object {

        @MainThread
        fun newIntent(
            context: Context,
            podcast: UIPodcast,
            startPosition: Long
        ) =
            Intent(context, AudioService::class.java).apply {
                putExtra(ARG_PODCAST, podcast)
                putExtra(ARG_START_POSITION, startPosition)
            }

        @MainThread
        fun newIntent(
            context: Context
        ) =
            Intent(context, AudioService::class.java)

    }

    private lateinit var exoPlayer: SimpleExoPlayer

    private var playbackTimer: Timer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    var uiPodcast: UIPodcast? = null

    private val _playerStatusLiveData = MutableLiveData<PlayerStatus>()
    val playerStatusLiveData: LiveData<PlayerStatus>
        get() = _playerStatusLiveData

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_SPEECH)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, true)
        exoPlayer.addListener(PlayerEventListener())

        // Setup notification and media session.
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            applicationContext,
            PLAYBACK_CHANNEL_ID,
            R.string.app_name,
            //R.string.playback_channel_name,
            PLAYBACK_NOTIFICATION_ID,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): String {
                    return uiPodcast?.title ?: "..."
                }

                @Nullable
                override fun createCurrentContentIntent(player: Player): PendingIntent? =
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        Intent(applicationContext, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                @Nullable
                override fun getCurrentContentText(player: Player): String? {
                    return null
                }

                @Nullable
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return getBitmapFromVectorDrawable(applicationContext, R.drawable.logotype)
                }
            },
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationStarted(
                    notificationId: Int,
                    notification: Notification?
                ) {
                    startForeground(notificationId, notification)
                }

                override fun onNotificationCancelled(notificationId: Int) {
                    _playerStatusLiveData.value = PlayerStatus.Cancelled(uiPodcast?.id)

                    stopSelf()
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification?,
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
        ).apply {
            // Omit skip previous and next actions.
            setUseNavigationActions(false)

            // Add stop action.
            setUseStopAction(true)

            val incrementMs = 30000L
            setFastForwardIncrementMs(incrementMs)
            setRewindIncrementMs(incrementMs)

            setPlayer(exoPlayer)
        }

        // Show lock screen controls and let apps like Google assistant manager playback.
        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG).apply {
            isActive = true
        }
        playerNotificationManager?.setMediaSessionToken(mediaSession?.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                override fun getMediaDescription(
                    player: Player,
                    windowIndex: Int
                ): MediaDescriptionCompat {
                    val bitmap =
                        getBitmapFromVectorDrawable(applicationContext, R.drawable.logotype)
                    val extras = Bundle().apply {
                        putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                        putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
                    }

                    val title = uiPodcast?.title ?: "..."

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

    override fun onBind(intent: Intent?): IBinder {
        handleIntent(intent)
        return AudioServiceBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        cancelPlaybackMonitor()

        mediaSession?.release()
        mediaSessionConnector?.setPlayer(null)
        playerNotificationManager?.setPlayer(null)

        exoPlayer.release()

        super.onDestroy()
    }

    @MainThread
    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (intent.hasExtra(ARG_PODCAST)) {
                uiPodcast = intent.getSerializableExtra(ARG_PODCAST) as UIPodcast
                val startPosition =
                    intent.getLongExtra(ARG_START_POSITION, C.POSITION_UNSET.toLong())
                val playbackSpeed = 1f

                play(uiPodcast?.audio?.audio_url, startPosition, playbackSpeed)

                Timber.w("AudioService - Playing podcast id %d", uiPodcast?.id)
            }

        } ?: Timber.w("AudioService - Podcast was not set.")

    }

    @MainThread
    fun play(uri: String?, startPosition: Long, playbackSpeed: Float? = null) {
        val userAgent = Util.getUserAgent(applicationContext, BuildConfig.APPLICATION_ID)
        val mediaSource = ExtractorMediaSource(
            Uri.parse(uri),
            DefaultDataSourceFactory(applicationContext, userAgent),
            DefaultExtractorsFactory(),
            null,
            null
        )

        val haveStartPosition = startPosition != C.POSITION_UNSET.toLong()
        if (haveStartPosition) {
            exoPlayer.seekTo(startPosition)
        }

        playbackSpeed?.let { changePlaybackSpeed(playbackSpeed) }

        exoPlayer.prepare(mediaSource, !haveStartPosition, false)
        exoPlayer.playWhenReady = true
    }

    @MainThread
    fun resume() {
        exoPlayer.playWhenReady = true
    }

    @MainThread
    fun pause() {
        exoPlayer.playWhenReady = false
    }

    @MainThread
    fun changePlaybackSpeed(playbackSpeed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    @MainThread
    private fun saveLastListeningPosition() {
        // Do we need this?
    }

    @MainThread
    private fun monitorPlaybackProgress() {
        if (playbackTimer == null) {
            playbackTimer = Timer()

            playbackTimer?.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        saveLastListeningPosition()

                        runOnUiThread {
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
        saveLastListeningPosition()

        playbackTimer?.cancel()
        playbackTimer = null
    }

    @MainThread
    private fun getBitmapFromVectorDrawable(
        context: Context,
        @DrawableRes drawableId: Int
    ): Bitmap? {
        return ContextCompat.getDrawable(context, drawableId)?.let {
            val drawable = DrawableCompat.wrap(it).mutate()

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        }
    }

    private inner class PlayerEventListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                if (exoPlayer.playWhenReady) {
                    uiPodcast?.let { _playerStatusLiveData.value = PlayerStatus.Playing(it.id) }
                } else {// Paused
                    uiPodcast?.let { _playerStatusLiveData.value = PlayerStatus.Paused(it.id) }
                }
            } else if (playbackState == Player.STATE_ENDED) {
                uiPodcast?.let { _playerStatusLiveData.value = PlayerStatus.Ended(it.id) }
            } else {
                uiPodcast?.let { _playerStatusLiveData.value = PlayerStatus.Other(it.id) }
            }

            // Only monitor playback to record progress when playing.
            if (playbackState == Player.STATE_READY && exoPlayer.playWhenReady) {
                monitorPlaybackProgress()
            } else {
                cancelPlaybackMonitor()
            }
        }

        override fun onPlayerError(e: ExoPlaybackException?) {
            uiPodcast?.let { _playerStatusLiveData.value = PlayerStatus.Error(it.id, e) }
        }

    }

}