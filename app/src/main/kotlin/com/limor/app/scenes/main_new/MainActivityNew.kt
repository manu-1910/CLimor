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
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.android.billingclient.api.*
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.databinding.ActivityMainNewBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.dm.ChatManager
import com.limor.app.dm.ChatRepository
import com.limor.app.dm.SessionsViewModel
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.scenes.main_new.view_model.MainViewModel
import com.limor.app.scenes.utils.*
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main_new.view_model.MainActivityViewModel
import com.limor.app.scenes.utils.ActivityPlayerViewManager
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.AudioService
import com.limor.app.service.PlayerBinder
import com.limor.app.uimodels.TagUIModel
import com.limor.app.util.AppNavigationManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import kotlinx.coroutines.*
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject

class MainActivityNew : AppCompatActivity(), HasSupportFragmentInjector, PlayerViewManager {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    lateinit var binding: ActivityMainNewBinding
    lateinit var playerBinding: ContainerWithSwipeablePlayerBinding

    @Inject
    lateinit var chatManager: ChatManager

    @Inject
    lateinit var infoRepository: GeneralInfoRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
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
        setupFabClickListener()
        setUpBottomNavigation()
        setupDefaultValues()

        checkAppVersion()

        activityPlayerViewManager =
            ActivityPlayerViewManager(supportFragmentManager, playerBinding, playerBinder)

        listenToChat()
    }

    private fun listenToChat() {
        chatRepository.getSessions().asLiveData().observe(this) { chatSessions ->
            val unreadCount = chatSessions.map { it.session.unreadCount ?: 0 }.sum()
            if (unreadCount > 0) {
                navigation.getOrCreateBadge(R.id.navigation_direct_messenger).apply {
                    isVisible = true
                    number = unreadCount
                }
            } else {
                navigation.removeBadge(R.id.navigation_direct_messenger)
            }
        }
    }

    private fun checkAppVersion() {
        model.checkAppVersion(PLATFORM).observe(this) { res ->
            res?.let { data ->
                Timber.d("VERISON -> $data")
                val versionOnBE = data.version?.toIntOrNull() ?: 9
                if (versionOnBE > BuildConfig.VERSION_CODE) {

                    //Launch Dialog by priority
                    //showUpdateDialog("", res.priority!!)
                    val appUpdateManager = AppUpdateManagerFactory.create(this)

                    // Returns an intent object that you use to check for an update.
                    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
                    val priority = data.priority ?: 0
                    // Get the priority to Define App Update Type
                    val updateType =
                        if (priority == 5) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
                    // Checks that the platform will allow the specified type of update.
                    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                        Timber.d("VERISON -> $appUpdateInfo")
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(updateType)
                        ) {
                            // Request the update.

                            //Use a snackBar or the dialog
                            appUpdateManager.startUpdateFlowForResult(
                                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                                appUpdateInfo,
                                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                                updateType,
                                // The current activity making the update request.
                                this,
                                // Include a request code to later monitor this update request.
                                5000)
                        } else {
                            Timber.d("VERISON -> update not supported")

                        }
                    }.addOnFailureListener {
                        Timber.e(it)
                        Timber.d("VERISON -> $it  exception")

                    }

                }

            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 5000) {
            if (resultCode == RESULT_OK) {
                binding.bottomBar.snackbar("New Version Has been Installed")
            } else {
                Timber.e("LIMOR -> Update flow failed! Result code: $resultCode")
            }
        }

    }

    private fun showUpdateDialog(versionName: String, priority: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_force_update, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)
        val dialog: AlertDialog = dialogBuilder.create()

        if (priority >= 1) {
            //Mark as mandatory update
            dialogView.cancelButton.visibility = View.GONE
        }

        dialogView.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogView.okButton.setOnClickListener {
            //Taking to play store
            try{
                startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(CommonsKt.APP_URI)))
            }catch (e: ActivityNotFoundException){
                startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(CommonsKt.APP_URL)))
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
                    return true
                }

                // Try to navigate to the destination within the current back stack
                if (navController.popBackStack(item.itemId, inclusive = false, saveState = true)) {
                    return true
                }

                // At this point we just have to navigate to the destination
                navController.navigate(item.itemId)

                // always select the item regardless of how the navigation was handled
                return true
            }
        })
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
        showPlayer(
            PlayerViewManager.PlayerArgs(
                PlayerViewManager.PlayerType.EXTENDED,
                castId
            )
        )
    }

    private fun checkPodCastDynamicLink() {
        val castId = PrefsHandler.getPodCastIdOfSharedLink(this)
        if (castId != 0) {
            PrefsHandler.savePodCastIdOfSharedLink(this, 0)
            openDynamicLinkPodcast(castId)

        } else {
            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // even if the function implementation says otherwise the pendingDynamicLinkData
                    // can be null, so do not remove the .? below
                    pendingDynamicLinkData?.link?.getQueryParameter("id")?.toInt()?.let {
                        openDynamicLinkPodcast(it)
                    }
                }
                .addOnFailureListener(this) { e ->
                    Timber.e(e)
                }
        }
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
            if (castId != 0) {
                Handler().postDelayed({
                    openExtendedPlayer(castId)
                }, 500)
            }
        }
        checkPodCastDynamicLink()
    }

    companion object{
        val PLATFORM = "and"
    }

}