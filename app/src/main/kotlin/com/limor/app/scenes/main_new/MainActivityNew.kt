package com.limor.app.scenes.main_new

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.android.billingclient.api.*
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.limor.app.R
import com.limor.app.databinding.ActivityMainNewBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.dm.ChatManager
import com.limor.app.dm.SessionsViewModel
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main_new.view_model.MainActivityViewModel
import com.limor.app.scenes.utils.ActivityPlayerViewManager
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.scenes.utils.showExtendedPlayer
import com.limor.app.service.AudioService
import com.limor.app.service.PlayBillingHandler
import com.limor.app.service.PlayerBinder
import com.limor.app.uimodels.TagUIModel
import com.limor.app.util.AppNavigationManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.coroutines.*
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
    lateinit var playerBinder: PlayerBinder

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
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

        activityPlayerViewManager =
            ActivityPlayerViewManager(supportFragmentManager, playerBinding, playerBinder)
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

}