package com.limor.app.scenes.main_new

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.android.billingclient.api.*
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.common.BaseActivity
import com.limor.app.databinding.ActivityMainNewBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.dm.ChatManager
import com.limor.app.dm.ChatRepository
import com.limor.app.dm.SessionsViewModel
import com.limor.app.events.OpenSharedPodcastEvent
import com.limor.app.extensions.requireTag
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.scenes.main_new.view_model.MainViewModel
import com.limor.app.scenes.utils.*
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main_new.fragments.comments.RootCommentsFragment
import com.limor.app.scenes.main_new.view_model.MainActivityViewModel
import com.limor.app.scenes.utils.ActivityPlayerViewManager
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.AudioService
import com.limor.app.service.PlayerBinder
import com.limor.app.uimodels.TagUIModel
import com.limor.app.util.AppNavigationManager
import com.onesignal.OneSignal
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject

class MainActivityNew : BaseActivity(), HasSupportFragmentInjector, PlayerViewManager {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    lateinit var binding: ActivityMainNewBinding
    lateinit var playerBinding: ContainerWithSwipeablePlayerBinding

    @Inject
    lateinit var chatManager: ChatManager

    @Inject
    lateinit var infoRepository: GeneralInfoRepository

    /*@Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory*/
    val model: MainViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var playerBinder: PlayerBinder

    @Inject
    lateinit var chatRepository: ChatRepository

    private val mainModel: MainActivityViewModel by viewModels { viewModelFactory }

    private var activityPlayerViewManager: ActivityPlayerViewManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Attach main activity binding into player container
        playerBinding = ContainerWithSwipeablePlayerBinding.inflate(layoutInflater)
        binding =
            ActivityMainNewBinding.inflate(layoutInflater, playerBinding.contentContainer, true)

        setContentView(playerBinding.root)

        setNavigationGraph()

        setupFabClickListener()
        setUpBottomNavigation()
        setupDefaultValues()

        checkAppVersion()

        activityPlayerViewManager =
            ActivityPlayerViewManager(supportFragmentManager, playerBinding, playerBinder)

        listenToChat()
    }

    private fun setNavigationGraph() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.main_nav_new)
        if(PrefsHandler.getJustLoggedIn(this)){
            navGraph.setStartDestination(R.id.navigation_discover)
        } else{
            navGraph.setStartDestination(R.id.navigation_home)
        }
        navController.graph = navGraph
    }

    private fun listenToChat() {
        chatRepository.getSessions().asLiveData().observe(this) { chatSessions ->
            val unreadCount = chatSessions.map { it.session.unreadCount ?: 0 }.sum()
            if (BuildConfig.DEBUG) {
                println("Unread count in total -> $unreadCount")
            }
            if (unreadCount > 0) {
                navigation.getOrCreateBadge(R.id.navigation_direct_messenger).apply {
                    isVisible = true
                    number = unreadCount
                    backgroundColor = 0xFFFB3E32.toInt()
                }
            } else {
                navigation.removeBadge(R.id.navigation_direct_messenger)
            }
        }
    }

    private fun onAppVersion(versionCode: Int, priority: Int) {
        if (versionCode <= BuildConfig.VERSION_CODE) {
            return
        }

        //Launch Dialog by priority
        //showUpdateDialog("", res.priority!!)
        val appUpdateManager = AppUpdateManagerFactory.create(this)

        // Get the priority to Define App Update Type
        val updateType = if (priority == 5)
            AppUpdateType.IMMEDIATE
        else
            AppUpdateType.FLEXIBLE

        // Checks that the platform will allow the specified type of update.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

            if (BuildConfig.DEBUG) {
                Timber.d("appUpdateInfoTask -> $appUpdateInfo")
            }
            onUpdateInfo(appUpdateInfo, updateType, appUpdateManager)

        }.addOnFailureListener {
            Timber.e(it)

            if (BuildConfig.DEBUG) {
                Timber.d("Getting app update info failed -> $it exception")
            }
        }

    }

    private fun onUpdateInfo(appUpdateInfo: AppUpdateInfo, updateType: Int, manager: AppUpdateManager) {
        val isAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        val isAllowed = appUpdateInfo.isUpdateTypeAllowed(updateType)

        if (isAvailable && isAllowed) {
            // Request the update.

            try {
                manager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateType,
                    this,
                    UPDATE_REQUEST_CODE
                )
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }

        } else if (BuildConfig.DEBUG) {
            Timber.d("App update: update not supported.")
        }
    }

    private fun checkAppVersion() {
        model.checkAppVersion(PLATFORM).observe(this) { res ->
            if (BuildConfig.DEBUG) {
                Timber.d("Got app version $res")
            }
            res?.let { data ->
                if (BuildConfig.DEBUG) {
                    Timber.d("App VERSION -> $data")
                }

                onAppVersion(data.buildNumber, data.priority)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {

            if (BuildConfig.DEBUG) {
                Timber.d("Got result from update task: $data")
            }

            if (resultCode == RESULT_OK) {
                // binding.bottomBar.snackbar(getString(R.string.updated_app_toast))

            } else if (BuildConfig.DEBUG) {
                Timber.e("LIMOR -> Update flow cancelled! Result code: $resultCode")

            }
        }
    }

    private fun showUpdateDialog(versionName: String, priority: Int) {
        LimorDialog(layoutInflater).apply {
            setTitle(R.string.purchase_cast_title)
            setMessage(R.string.purchase_cast_description_for_comment)
            setIcon(R.drawable.ic_comment_purchase)
            addButton(R.string.cancel, false)
            addButton(R.string.buy_now, true) {

            }
        }.show()

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_force_update, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)
        val dialog: AlertDialog = dialogBuilder.create()

        if (priority == 5) {
            // Mark as mandatory update
            dialogView.cancelButton.visibility = View.GONE
        }

        dialogView.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogView.okButton.setOnClickListener {
            // Taking to play store
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CommonsKt.APP_URI)))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CommonsKt.APP_URL)))
            }
            dialog.dismiss()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset)
            show()
        }
    }

    private fun setupDefaultValues() {
        lifecycleScope.launchWhenCreated {
            JwtChecker.getUserIdFromJwt(false)?.let {
                PrefsHandler.saveCurrentUserId(this@MainActivityNew, it)
                chatManager.loginCurrentUser()
            }
        }
    }

    private fun setupFabClickListener() {
        binding.fab.setOnClickListener {
            navController.navigate(R.id.navigation_record)
        }
    }

    lateinit var navController: NavController

    private fun ensureSelected(current: Int, destinationId: Int) {
        if (current != destinationId) {
            return
        }
        navigation.selectedItemId = destinationId
    }

    private fun setUpBottomNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            ensureSelected(destination.id, R.id.navigation_home)
            ensureSelected(destination.id, R.id.navigation_discover)
            ensureSelected(destination.id, R.id.navigation_profile)
            ensureSelected(destination.id, R.id.navigation_direct_messenger)
        }

        // This allows for more control on what happens when the bottom bar buttons are clicked.
        navigation.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                if (item.itemId == navController.currentDestination?.id) {
                    ensureLayout()
                    return true
                }

                // Try to navigate to the destination within the current back stack
                if (navController.popBackStack(item.itemId, inclusive = false, saveState = true)) {
                    ensureLayout()
                    return true
                }

                // At this point we just have to navigate to the destination
                navController.navigate(item.itemId)

                ensureLayout()

                // always select the item regardless of how the navigation was handled
                return true
            }
        })
    }

    fun ensureLayout() {
        val manager = activityPlayerViewManager ?: return
        val host = binding.root.findViewById<View>(R.id.nav_host_fragment) ?: return

        val updateParams: () -> Unit = {
            host.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        manager.doAfterTransitionComplete {
            updateParams()
        }

        // However to avoid any edge cases we update the params again after the aniamtion has
        // surely completed.
        //
        // 600 ms because the transition animation duration is 500 + 100 ms slack
        // check container_transition_player_scene.xml for the animation duration value
        //
        host.postDelayed({
            updateParams()
        }, 600)

    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentInjector

    override fun isPlayerVisible() = activityPlayerViewManager?.isPlayerVisible() ?: false

    override fun showPlayer(args: PlayerViewManager.PlayerArgs, onTransitioned: (() -> Unit)?) {
        activityPlayerViewManager?.showPlayer(args, onTransitioned)
    }

    override fun isPlayingComment(audioTrack: AudioService.AudioTrack): Boolean {
        return activityPlayerViewManager?.isPlayingComment(audioTrack) == true
    }

    override fun isPlaying(audioTrack: AudioService.AudioTrack): Boolean {
        return activityPlayerViewManager?.isPlaying(audioTrack) == true
    }

    override fun hidePlayer() {
        activityPlayerViewManager?.hidePlayer()
    }

    override fun navigateToHashTag(hashtag: TagUIModel) {
        navController.apply {
            if (!popBackStack(R.id.navigation_discover, false)) {
                navigate(R.id.navigation_discover)
            }
            navigate(
                R.id.action_navigation_discover_to_discoverHashtagFragment,
                bundleOf(DiscoverHashtagFragment.HASHTAG_KEY to hashtag)
            )
        }
    }

    override fun playPreview(audio: AudioService.AudioTrack, startPosition: Int, endPosition: Int) {
        activityPlayerViewManager?.playPreview(audio, startPosition, endPosition)
    }

    override fun stopPreview(reset: Boolean) {
        activityPlayerViewManager?.stopPreview(reset)
    }

    fun stop(){
        activityPlayerViewManager?.stop()
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
                cast.owner?.let {
                    UserProfileActivity.show(this, it.username ?: "", it.id, 1)
                }

            } else {
                openCast(cast.id)
            }


        }
    }

    private fun openCast(castId: Int) {
        EventBus.getDefault().post(OpenSharedPodcastEvent(podcastId = castId))
        showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                castId
            )
        )
    }

    override fun onDestroy() {
        activityPlayerViewManager?.stop()
        activityPlayerViewManager = null
        super.onDestroy()
    }


    fun openExtendedPlayer(it: Int) {
        activityPlayerViewManager?.showExtendedPlayer(it)
    }

    override fun onResume() {
        super.onResume()
        intent.extras?.getInt(AppNavigationManager.CAST_KEY)?.let { castId ->
            intent.removeExtra(AppNavigationManager.CAST_KEY)
            if (castId != 0) {
                Handler().postDelayed({
                    openExtendedPlayer(castId)
                }, 500)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOpenSharedPodcastEvent(event: OpenSharedPodcastEvent) {
        if(activityPlayerViewManager?.isPlayingSameCast(event.podcastId) == false){
            showPlayer(
                PlayerViewManager.PlayerArgs(
                    PlayerViewManager.PlayerType.EXTENDED,
                    event.podcastId
                )
            )
        }
    }

    companion object{
        val PLATFORM = "and"
        val UPDATE_REQUEST_CODE = 5000
    }

}