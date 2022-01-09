package com.limor.app.scenes.patron.manage

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.limor.app.R
import com.limor.app.databinding.ActivityManagePatronBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.utils.ActivityPlayerViewManager
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.AudioService
import com.limor.app.service.PlayerBinder
import com.limor.app.uimodels.TagUIModel
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class ManagePatronActivity : AppCompatActivity(), HasSupportFragmentInjector, PlayerViewManager {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    lateinit var binding: ActivityManagePatronBinding

    lateinit var playerBinding: ContainerWithSwipeablePlayerBinding

    @Inject
    lateinit var playerBinder: PlayerBinder

    private var activityPlayerViewManager: ActivityPlayerViewManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerBinding = ContainerWithSwipeablePlayerBinding.inflate(layoutInflater)
        binding = ActivityManagePatronBinding.inflate(
            layoutInflater,
            playerBinding.contentContainer,
            true
        )
        setContentView(playerBinding.root)

        activityPlayerViewManager =
            ActivityPlayerViewManager(supportFragmentManager, playerBinding, playerBinder)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        if (intent.extras?.containsKey("invitations") == true) {
            navController.navigate(R.id.fragment_invite_users)
        }

    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector
    override fun isPlayerVisible(): Boolean {
        return activityPlayerViewManager?.isPlayerVisible() ?: false
    }

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
    }

    override fun playPreview(audio: AudioService.AudioTrack, startPosition: Int, endPosition: Int) {
    }

    override fun stopPreview(reset: Boolean) {

    }

}