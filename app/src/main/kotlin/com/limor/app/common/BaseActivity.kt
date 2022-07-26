package com.limor.app.common

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.events.OpenSharedPodcastEvent
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.fragments.player.AudioPlayerActivity
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.main_new.fragments.FragmentPodcastPopup
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view_model.MainActivityViewModel
import com.limor.app.scenes.splash.SplashActivity
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.AudioService
import com.limor.app.service.PlayerStatus
import com.limor.app.uimodels.CastUIModel
import com.onesignal.OneSignal
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.mini_player_view.*
import kotlinx.android.synthetic.main.mini_player_view.view.*
import org.greenrobot.eventbus.EventBus
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
    private val mainModel: MainActivityViewModel by viewModels { viewModelFactory }

    // For the AudioService
    var audioService: AudioService? = null
    private var miniPlayerView: RelativeLayout? = null
    private var playerStatus: PlayerStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
    }

    private var lastPlayingPosition = 0
    private var podcast: CastUIModel? = null


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.service

            /*audioService?.playerStatusLiveData?.observe(this@BaseActivity, Observer {

                playerStatus = it


                when (playerStatus) {
                    is PlayerStatus.Cancelled -> {
                        miniPlayerView!!.visibility = View.GONE
                        stopAudioService()
                    }
                    is PlayerStatus.Playing -> {
                        *//*if (miniPlayerView!!.tag != audioService?.uiPodcast?.id) {
                            miniPlayerView!!.tag = audioService?.uiPodcast?.id
                            setupMiniPlayerUi()
                        }*//*

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

            audioService?.currentPlayingPosition?.observe(this@BaseActivity, Observer { playingPosition ->
                lastPlayingPosition = playingPosition.toInt()
                try {
                    progress_audio_playback.progress = lastPlayingPosition
                } catch (ex: Exception) { ex.printStackTrace() }
            })*/


            /*// Show player after config change.
            val podcast = audioService?.uiPodcast
            if (podcast != null) {
                setupMiniPlayerUi()
            }*/
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
        }
    }

    private fun setupMiniPlayerUi() {
        /*try {
            miniPlayerView!!.visibility = View.VISIBLE
            Glide.with(miniPlayerView!!.iv_audio).load(audioService?.uiPodcast?.imageLinks?.small)
                .centerCrop().into(miniPlayerView!!.iv_audio)
            miniPlayerView!!.tv_audio_title.text = audioService?.uiPodcast?.caption

            val durationMillis = audioService?.uiPodcast?.audio?.totalLength

            miniPlayerView!!.progress_audio_playback.max = durationMillis ?: 0
            miniPlayerView!!.progress_audio_playback.progress = lastPlayingPosition

            miniPlayerView!!.tv_duration.text = Commons.getHumanReadableTimeFromMillis(durationMillis!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

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
                //podcastDetailsIntent.putExtra("podcast", audioService?.uiPodcast)
                podcastDetailsIntent.putExtra("commenting", true)
                //podcastDetailsIntent.putExtra("position", audioService?.feedPosition)
                startActivityForResult(podcastDetailsIntent, MainActivity.REQUEST_AUDIO_PLAYER)
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
                        /*setPlayerUiPaused()
                        audioService?.play(
                            audioService?.uiPodcast,
                            1L,
                            1F
                        )*/
                    }
                }

            }

            miniPlayerView!!.fl_launch_maximised_player.onClick {
                /*audioService?.let {
                    if(podcast != null) it.uiPodcast = podcast
                }
                val audioPlayerIntent = Intent(this, AudioPlayerActivity::class.java)
                audioPlayerIntent.putExtra(
                    AudioPlayerActivity.BUNDLE_KEY_PODCAST,
                    audioService?.uiPodcast
                )
                startActivityForResult(audioPlayerIntent, MainActivity.REQUEST_AUDIO_PLAYER)
                overridePendingTransition(R.anim.push_up_in_enter_no_alpha, 0)*/
            }
        }

        if(this::class in setOf(SplashActivity::class, MainActivityNew::class)){
            checkPendingOnSignalNotifications()
            setOneSignalNotificationHandler()
            checkDynamicLink()
        }
    }

    private fun checkPendingOnSignalNotifications(){
        val userId = PrefsHandler.getUserIdFromOneSignalNotification(this)
        val userName = PrefsHandler.getUserNameFromOneSignalNotification(this) ?: ""
        val tabId = PrefsHandler.getUserTabIdFromOneSignalNotification(this)
        val commentId = PrefsHandler.getCommentId(this)
        val childCommentId = PrefsHandler.getChildCommentId(this)
        if(commentId != -1){
            val castId: Int = PrefsHandler.getCommentCastId(this)
            PrefsHandler.setCommentId(this, -1)
            PrefsHandler.setChildCommentId(this, -1)
            PrefsHandler.setCommentCastId(this, -1)
            mainModel.loadCast(castId).observe(this){
                it?.let { it1 ->
                    RootCommentsFragment.newInstance(it1, commentId, childCommentId).also { fragment ->
                        fragment.show(supportFragmentManager, fragment.requireTag())
                    }
                }
            }
        }
        else if(userId != 0){
            UserProfileActivity.show(this, userName, userId,tabId)
            PrefsHandler.saveUserIdFromOneSignalNotification(this, 0)
            PrefsHandler.saveUserNameFromOneSignalNotification(this, "")
            PrefsHandler.saveUserTabIdFromOneSignalNotification(this, 0)
        } else{
            checkFirebasePendingDynamicLink()
        }
    }

    private fun checkFirebasePendingDynamicLink(){
        val podcastId = PrefsHandler.getPodCastIdOfSharedLink(this)
        if(podcastId > 0){
            PrefsHandler.savePodCastIdOfSharedLink(this, -1)
            openDynamicLinkPodcast(podcastId)
        }
    }

    private fun setOneSignalNotificationHandler(){
        OneSignal.setNotificationOpenedHandler { result ->
            if (BuildConfig.DEBUG) {
                println("N.Payload ${result.toJSONObject()}")
            }
            if(result.notification.additionalData.get("commentId").toString() != "null"){
                val commentId = result.notification.additionalData.get("commentId").toString().toInt()
                val childCommentId = if(result.notification.additionalData.get("childCommentId").toString() == "null") {
                    -1
                } else{
                    result.notification.additionalData.get("childCommentId").toString().toInt()
                }
                val castId: Int = result.notification.additionalData.get("targetId").toString().toInt()
                if(this::class in setOf(SplashActivity::class, AuthActivityNew::class)){
                    PrefsHandler.setCommentId(this, result.notification.additionalData.get("commentId").toString().toInt())
                    PrefsHandler.setChildCommentId(this,  childCommentId)
                    PrefsHandler.setCommentCastId(this, castId)
                } else{
                    openCommentsSections(commentId, childCommentId, castId)
                }
            } else{
                val id: Int = result.notification.additionalData.getString("targetId").toInt()
                var tabId = 0
                if (result.notification.additionalData.has("notificationType") && result.notification.additionalData.getString("notificationType") == "patronRequest") {
                    tabId = 1
                }
                if(result.notification.additionalData.getString("targetType").equals("user")){
                    if(this::class in setOf(SplashActivity::class, AuthActivityNew::class)){
                        PrefsHandler.saveUserIdFromOneSignalNotification(this, id)
                        PrefsHandler.saveUserNameFromOneSignalNotification(this, result.notification.additionalData.getString("initiatorUsername"))
                        PrefsHandler.saveUserTabIdFromOneSignalNotification(this, tabId)
                    } else{
                        UserProfileActivity.show(this,result.notification.additionalData.getString("initiatorUsername"),id,tabId)
                    }
                } else{
                    if(this::class in setOf(SplashActivity:: class, AuthActivityNew::class)){
                        PrefsHandler.savePodCastIdOfSharedLink(this, id)
                    } else{
                        openDynamicLinkPodcast(id)
                    }
                }
            }
        }
    }

    private fun checkDynamicLink(){
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val td: Int? = deepLink?.getQueryParameter("id")?.toInt()
                    td?.let {
                        if(this::class in setOf(SplashActivity::class, AuthActivityNew::class)){
                            PrefsHandler.savePodCastIdOfSharedLink(this, it)
                        } else{
                            openDynamicLinkPodcast(it)
                        }
                    }

                    Timber.d("DeepLink fetched $deepLink")
                }
            }
            .addOnFailureListener(this) { e -> Timber.e(e) }
    }

    fun openCommentsSections(commentId: Int, childCommentId: Int, castId: Int){
        mainModel.loadCast(castId).observe(this){
            it?.let { it1 ->
                RootCommentsFragment.newInstance(it1, commentId, childCommentId).also { fragment ->
                    fragment.show(supportFragmentManager, fragment.requireTag())
                }
            }
        }
    }

    private fun openDynamicLinkPodcast(castId: Int) {
        println("Opening Dynamic Podcast")
        val currentUserId = PrefsHandler.getCurrentUserId(this)
        mainModel.loadCast(castId).observe(this) { cast ->
            if (cast == null) {
                return@observe
            }

            if (cast.owner?.id == currentUserId) {
                openCast(cast.id)
            } else if (cast.patronCast == true && cast.patronDetails?.purchased == false) {
                val dialog = FragmentPodcastPopup.newInstance(cast.id)
                dialog.show(supportFragmentManager, FragmentPodcastPopup.TAG)
            } else {
                openCast(cast.id)
            }
        }
    }

    private fun openCast(castId: Int) {
        if(this is PlayerViewManager){
            EventBus.getDefault().post(OpenSharedPodcastEvent(podcastId = castId))
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                EventBus.getDefault().post(OpenSharedPodcastEvent(podcastId = castId))
            }, 1500)
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
            /*AudioService.newIntent(this).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }*/
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