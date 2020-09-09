package io.square1.limor.scenes.main.fragments.player

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.exoplayer2.ui.PlayerControlView
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.R
import io.square1.limor.service.AudioService
import io.square1.limor.service.PlayerStatus
import io.square1.limor.uimodels.UIPodcast
import kotlinx.android.synthetic.main.exo_player_control_view.view.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.toast
import java.lang.Exception
import javax.inject.Inject


class AudioPlayerActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    private lateinit var navController: NavController

    // For the AudioService
    private var audioService: AudioService? = null
    private var playerControlView: PlayerControlView? = null
    private var playerStatus: PlayerStatus? = null
    private var uiPodcast: UIPodcast? = null

    companion object {
        val TAG: String = AudioPlayerActivity::class.java.simpleName
        const val BUNDLE_KEY_PODCAST = "BUNDLE_KEY_PODCAST"
        fun newInstance() = AudioPlayerActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service
            playerControlView!!.player = binder.exoPlayer

            audioService?.playerStatusLiveData?.observe(this@AudioPlayerActivity, Observer {

                playerStatus = it

                when (playerStatus) {
                    is PlayerStatus.Cancelled -> {
                        stopAudioService()
                        finish()
                    }
                    is PlayerStatus.Playing -> {

                    }
                    is PlayerStatus.Paused -> {

                    }
                    is PlayerStatus.Ended -> {

                    }
                    is PlayerStatus.Error -> {
                        toast(getString(R.string.audio_player_error_msg))
                    }
                }

            })

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_audio_player)

        //Just in case
        try {
            btnClose.rotation = 270F
            val bundle = intent?.extras
            uiPodcast = bundle?.get(BUNDLE_KEY_PODCAST) as UIPodcast?
        }catch (e: Exception){
            e.printStackTrace()
        }

        setupNavigationController()
        configureToolbar()
    }

    override fun onStart() {
        super.onStart()

        playerControlView = findViewById(R.id.player_control_view)

        if (playerControlView != null) {
            if (isServiceRunning(AudioService::class.java)) {
                bindToAudioService()
            }
        }

        if (playerControlView != null) {
            playerControlView?.iv_bookmark?.onClick { toast("Bookmark from exoPlayer clicked") }

            playerControlView?.exo_ffwd_limor?.onClick {
                audioService?.forward()
            }

            playerControlView?.exo_rew_limor?.onClick {
                audioService?.rewind()
            }
        }

    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.push_down_out_pop_exit_no_alpha)
    }


    private fun bindToAudioService() {
        if (audioService == null) {
            AudioService.newIntent(this).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun unbindAudioService() {
        if (audioService != null) {
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


    private fun setupNavigationController() {
        navController =
            Navigation.findNavController(this, R.id.navigation_host_fragment_audio_player)
    }

    private fun configureToolbar() {

        //Toolbar Left
        btnClose.onClick {
            finish()
        }

    }

}