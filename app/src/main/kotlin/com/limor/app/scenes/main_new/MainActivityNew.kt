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
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.apollo.GeneralInfoRepository
import com.limor.app.databinding.ActivityMainNewBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.dm.ChatManager
import com.limor.app.scenes.auth_new.util.JwtChecker
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.scenes.main_new.view_model.MainViewModel
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
    lateinit var infoRepository: GeneralInfoRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val model: MainViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var playerBinder: PlayerBinder




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
    }

    private fun checkAppVersion() {
            model.checkAppVersion(PLATFORM).observe(this){
                res ->
                res?.version?.let{
                    version ->
                    if(BuildConfig.VERSION_CODE < version.toInt()){
                        //Launch Dialog by priority
                    }
                }

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

    override fun stopPreview() {
        activityPlayerViewManager?.stopPreview()
    }

    fun checkPodCastDynamicLink() {
        val castId = PrefsHandler.getPodCastIdOfSharedLink(this)
        if (castId != 0) {
            castId.let {
                showPlayer(
                    PlayerViewManager.PlayerArgs(
                        PlayerViewManager.PlayerType.EXTENDED,
                        it
                    )
                )
            }
            PrefsHandler.savePodCastIdOfSharedLink(this, 0)
        } else {
            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    val deepLink: Uri?
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                        val td: Int = deepLink?.getQueryParameter("id")?.toInt()!!
                        td.let {
                            showPlayer(
                                PlayerViewManager.PlayerArgs(
                                    PlayerViewManager.PlayerType.EXTENDED,
                                    td
                                )
                            )
                        }
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