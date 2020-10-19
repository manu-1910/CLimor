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
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.square1.limor.R
import io.square1.limor.scenes.main.fragments.player.AudioPlayerActivity
import io.square1.limor.scenes.main.fragments.podcast.PodcastDetailsActivity
import io.square1.limor.scenes.main.viewmodels.CreatePodcastDropOffViewModel
import io.square1.limor.scenes.utils.Commons
import io.square1.limor.service.AudioService
import io.square1.limor.service.PlayerStatus
import io.square1.limor.uimodels.UIPodcast
import kotlinx.android.synthetic.main.mini_player_view.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.toast
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // For the AudioService
    private var audioService: AudioService? = null
    private var miniPlayerView: RelativeLayout? = null
    private var playerStatus: PlayerStatus? = null


    private lateinit var viewModelCreatePodcastDropOff : CreatePodcastDropOffViewModel
    private val createCommentDropOffDataTrigger = PublishSubject.create<Unit>()
    private var lastProgressTrackedPodcast : UIPodcast? = null
    private var lastProgressTrackedTen : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        bindViewModel()
        initApiCallCreatePodcastDropOff()
    }

    private fun bindViewModel() {
        viewModelCreatePodcastDropOff = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CreatePodcastDropOffViewModel::class.java)
    }

    private fun initApiCallCreatePodcastDropOff() {
        val output = viewModelCreatePodcastDropOff.transform(
            CreatePodcastDropOffViewModel.Input(
                createCommentDropOffDataTrigger
            )
        )

        output.response.observe(this, Observer {
            Timber.d("Dropoff of podcast ${lastProgressTrackedPodcast?.id} and ten $lastProgressTrackedTen sent successfully ")
        })

        output.errorMessage.observe(this, Observer {
            Timber.d("Error sending dropoff of podcast ${lastProgressTrackedPodcast?.id} and ten $lastProgressTrackedTen")
        })
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
                        onPodcastPlayingStopped()
                    }
                    is PlayerStatus.Error -> {
                        toast(getString(R.string.audio_player_error_msg))
                    }
                }

            })

            audioService?.currentPlayingPosition?.observe(this@BaseActivity, Observer{position ->
                onPlayingPodcsatPositionChanged(position)
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

    private fun onPodcastPlayingStopped() {
        val podcastDurationSec = audioService?.uiPodcast?.audio?.total_length?.toFloat() ?: 0.0f
        val duration : Double = podcastDurationSec * 1000.0
        val position = audioService?.currentPlayingPosition?.value ?: 0
        val currentPercentage = position.toFloat() * 100f / duration.toFloat()
        audioService?.uiPodcast?.id?.let {
            viewModelCreatePodcastDropOff.idPodcast = it
            viewModelCreatePodcastDropOff.percentage = currentPercentage
            createCommentDropOffDataTrigger.onNext(Unit)
        }
    }

    private fun onPlayingPodcsatPositionChanged(position: Long) {
        checkIfDropOffAndSendIfNeeded(position)
    }

    private fun checkIfDropOffAndSendIfNeeded(position: Long) {
        audioService?.uiPodcast?.audio?.total_length?.let {duration ->

            val currentPercentage = position.toFloat() * 100f / duration.toFloat()

            // this is the current 'ten' ('decena' in Spanish).
            // example:
            //   50 out of 100 is 50%, it would return 5
            //   27 out of 100 is 27%, it would return 2
            //   40 out of 200 is 20%, it would return 2 again
            val currentTen = (currentPercentage / 10).toInt()
            if(lastProgressTrackedPodcast != audioService?.uiPodcast) {
                lastProgressTrackedPodcast = audioService?.uiPodcast
            } else {
                if(currentTen > lastProgressTrackedTen) {
                    viewModelCreatePodcastDropOff.idPodcast = audioService?.uiPodcast?.id ?: 0
                    viewModelCreatePodcastDropOff.percentage = currentPercentage
                    createCommentDropOffDataTrigger.onNext(Unit)
                }
            }
            lastProgressTrackedTen = currentTen

//            Timber.d("the new playing position is $position")
        }
    }

    private fun setupMiniPlayerUi() {
        try {
            miniPlayerView!!.visibility = View.VISIBLE
            Glide.with(miniPlayerView!!.iv_audio).load(audioService?.uiPodcast?.images?.small_url)
                .centerCrop().into(miniPlayerView!!.iv_audio)
            miniPlayerView!!.tv_audio_title.text = audioService?.uiPodcast?.caption

            val durationMillis = audioService?.uiPodcast?.audio?.total_length?.toInt()
//            val durationMillis = audioService?.uiPodcast?.audio?.duration
            miniPlayerView!!.tv_duration.text = Commons.getHumanReadableTimeFromMillis(durationMillis!!)
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