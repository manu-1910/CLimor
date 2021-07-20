package com.limor.app.scenes.main_new

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.limor.app.R
import com.limor.app.databinding.ActivityMainNewBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.scenes.utils.ActivityPlayerViewManager
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main_new.*
import javax.inject.Inject

class MainActivityNew : AppCompatActivity(), HasSupportFragmentInjector, PlayerViewManager {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    lateinit var binding: ActivityMainNewBinding
    lateinit var playerBinding: ContainerWithSwipeablePlayerBinding

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

        activityPlayerViewManager =
            ActivityPlayerViewManager(supportFragmentManager, playerBinding, playerBinder)
    }

    private fun setupFabClickListener() {
        binding.fab.setOnClickListener {
            navController.navigate(R.id.navigation_record)
        }
    }

    lateinit var navController: NavController

    private fun setUpBottomNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navigation.setupWithNavController(navController)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentInjector

    override fun isPlayerVisible() = activityPlayerViewManager?.isPlayerVisible() ?: false

    override fun showPlayer(args: PlayerViewManager.PlayerArgs) {
        activityPlayerViewManager?.showPlayer(args)
    }

    override fun hidePlayer() {
        activityPlayerViewManager?.hidePlayer()
    }

    override fun onDestroy() {
        activityPlayerViewManager?.stop()
        activityPlayerViewManager = null
        super.onDestroy()
    }
}