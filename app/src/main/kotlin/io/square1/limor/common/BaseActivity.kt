package io.square1.limor.common

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import dagger.android.AndroidInjection
import io.square1.limor.R
import io.square1.limor.scenes.main.fragments.player.AudioPlayerActivity
import io.square1.limor.scenes.main.fragments.podcast.PodcastDetailsActivity
import io.square1.limor.service.AudioService
import io.square1.limor.service.PlayerStatus
import kotlinx.android.synthetic.main.mini_player_view.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.toast
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // For the AudioService
    private var audioService: AudioService? = null
    private var miniPlayerView: RelativeLayout? = null
    private var playerStatus: PlayerStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service

            audioService?.playerStatusLiveData?.observe(this@BaseActivity, Observer {

                playerStatus = it


                when (playerStatus) {
                    is PlayerStatus.Cancelled -> {
                        miniPlayerView!!.visibility = View.GONE
                        stopAudioService()
                    }
                    is PlayerStatus.Playing -> {
                        if (miniPlayerView!!.tag != audioService?.uiPodcast?.id) {
                            miniPlayerView!!.tag = audioService?.uiPodcast?.id
                            setupMiniPlayerUi()
                        }

                        setPlayerUiPlaying()
                    }
                    is PlayerStatus.Paused -> {
                        setPlayerUiPaused()
                    }
                    is PlayerStatus.Ended -> {
                        setPlayerUiPaused()
                    }
                    is PlayerStatus.Error -> {
                        toast(getString(R.string.audio_player_error_msg))
                    }
                }

            })

            // Show player after config change.
            val podcast = audioService?.uiPodcast
            if (podcast != null) {
                setupMiniPlayerUi()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    private fun setupMiniPlayerUi() {
        try {
            miniPlayerView!!.visibility = View.VISIBLE
            Glide.with(miniPlayerView!!.iv_audio).load(audioService?.uiPodcast?.images?.small_url)
                .centerCrop().into(miniPlayerView!!.iv_audio)
            miniPlayerView!!.tv_audio_title.text = audioService?.uiPodcast?.caption

            val durationMillis = audioService?.uiPodcast?.audio?.duration
            val minutes = durationMillis!! / 1000 / 60
            val seconds = durationMillis / 1000 % 60
            val humanReadableDuration = String.format("%dm %ds", minutes, seconds)
            miniPlayerView!!.tv_duration.text = humanReadableDuration
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onStart() {
        super.onStart()

        miniPlayerView = findViewById(R.id.rl_mini_player_view)

        if (miniPlayerView != null) {
            if (isServiceRunning(AudioService::class.java)) {
                bindToAudioService()
            } else {
                miniPlayerView!!.visibility = View.GONE
            }

            miniPlayerView!!.iv_close.onClick {
                stopAudioService()
                miniPlayerView!!.visibility = View.GONE
            }

            miniPlayerView!!.iv_comments.onClick {
                val podcastDetailsIntent =
                    Intent(it?.context, PodcastDetailsActivity::class.java)
                podcastDetailsIntent.putExtra("podcast", audioService?.uiPodcast)
                podcastDetailsIntent.putExtra("commenting", true)
                startActivity(podcastDetailsIntent)
            }

            miniPlayerView!!.iv_play_pause.onClick {

                when (playerStatus) {
                    is PlayerStatus.Playing -> {
                        audioService?.pause()
                        setPlayerUiPaused()
                    }
                    is PlayerStatus.Paused -> {
                        audioService?.resume()
                        setPlayerUiPlaying()
                    }
                    is PlayerStatus.Ended -> {
                        setPlayerUiPaused()
                        audioService?.play(
                            audioService?.uiPodcast?.audio?.audio_url,
                            1L,
                            1F
                        )
                    }
                }

            }

            miniPlayerView!!.fl_launch_maximised_player.onClick {

                val audioPlayerIntent = Intent(this, AudioPlayerActivity::class.java)
                audioPlayerIntent.putExtra(
                    AudioPlayerActivity.BUNDLE_KEY_PODCAST,
                    audioService?.uiPodcast
                )
                startActivity(audioPlayerIntent)
                overridePendingTransition(R.anim.push_up_in_enter_no_alpha, 0)
            }
        }

    }

    private fun setPlayerUiPlaying() {
        miniPlayerView!!.iv_play_pause.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pause
            )
        )
    }

    private fun setPlayerUiPaused() {
        miniPlayerView!!.iv_play_pause.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.play
            )
        )
    }

    @SuppressWarnings("deprecation")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onStop() {
        unbindAudioService()
        super.onStop()
    }


    private fun bindToAudioService() {
        if (audioService == null) {
            AudioService.newIntent(this).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        } else {
            setupMiniPlayerUi()
        }
    }

    private fun unbindAudioService() {
        if (miniPlayerView != null && audioService != null) {
            unbindService(connection)
            audioService = null
        }
    }

    fun stopAudioService() {
        audioService?.pause()

        unbindAudioService()
        stopService(Intent(this, AudioService::class.java))

        audioService = null
        playerStatus = null
    }

    fun showMiniPlayer() {
        if (miniPlayerView != null) {
            bindToAudioService()
        }
    }

    protected fun trackBackgroudProgress(isRunning: Boolean) {
        if (isRunning) {
            //showLoading()
        } else {
            //hideLoading()
        }
    }

    protected open fun showAlert(
        title: Int,
        message: Int,
        okAction: () -> Unit,
        cancelAction: () -> Unit
    ) {
        alert {
            if (title != 0) this.titleResource = title
            if (message != 0) this.messageResource = message
            okButton {
                okAction()
                it.dismiss()
            }
            noButton {
                cancelAction()
                it.dismiss()
            }
        }.show()
    }
}