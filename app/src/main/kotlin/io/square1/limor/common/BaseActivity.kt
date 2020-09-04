package io.square1.limor.common

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.ui.PlayerControlView
import dagger.android.AndroidInjection
import io.square1.limor.R
import io.square1.limor.service.AudioService
import io.square1.limor.service.PlayerStatus
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import timber.log.Timber
import javax.inject.Inject


abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var audioService: AudioService? = null
    private var playerControlView: PlayerControlView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service

            // Attach the ExoPlayer to the PlayerView.
            playerControlView!!.player = binder.exoPlayer

            // Pass player updates to interested observers.
            audioService?.playerStatusLiveData?.observe(this@BaseActivity, Observer {

//                _playerStatusLiveData.value = it
//                playerOverlayPlayMaterialButton.isSelected = it is PlayerStatus.Playing

                if (it is PlayerStatus.Cancelled) {
                    playerControlView!!.visibility = View.GONE
                    stopAudioService()
                }
            })

            // Show player after config change.
            val podcastId = audioService?.podcastId
            if (podcastId != null) {
                playerControlView!!.visibility = View.VISIBLE
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    override fun onStart() {
        super.onStart()

        playerControlView = findViewById(R.id.player_control_view)
        Timber.e("baz")
        // Show the player, if the audio service is already running.
        if(playerControlView != null){
            if (isServiceRunning(AudioService::class.java)) {
                bindToAudioService()
            } else {
                playerControlView!!.visibility = View.GONE
            }
        }

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
        }
    }

    private fun unbindAudioService() {
        if (playerControlView != null && audioService != null) {
            unbindService(connection)

            audioService = null
        }
    }

    private fun stopAudioService() {
        audioService?.pause()

        unbindAudioService()
        stopService(Intent(this, AudioService::class.java))

        audioService = null
    }

    fun showExoPlayerControls(){
        if(playerControlView != null){
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