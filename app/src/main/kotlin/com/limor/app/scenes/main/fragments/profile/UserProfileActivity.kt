package com.limor.app.scenes.main.fragments.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.databinding.ActivityProfileBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.scenes.utils.ActivityPlayerViewManager
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.AudioService
import com.limor.app.service.PlayerBinder
import com.limor.app.uimodels.TagUIModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import timber.log.Timber
import javax.inject.Inject


class UserProfileActivity : BaseActivity(), HasSupportFragmentInjector, PlayerViewManager {

//    var uiUser : UIUser? = null


    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    private val model: UserProfileViewModel by viewModels { viewModelFactory }

    lateinit var binding: ActivityProfileBinding


    lateinit var playerBinding: ContainerWithSwipeablePlayerBinding

    @Inject
    lateinit var playerBinder: PlayerBinder

    private var activityPlayerViewManager: ActivityPlayerViewManager? = null

    companion object {
        val TAG: String = UserProfileActivity::class.java.simpleName
        fun newInstance() = UserProfileActivity()

        fun show(context: Context, username: String, userId: Int, tab: Int = 0) {
            val userProfileIntent = Intent(context, UserProfileActivity::class.java)
            userProfileIntent.putExtra(UserProfileFragment.USER_NAME_KEY, username)
            userProfileIntent.putExtra(UserProfileFragment.USER_ID_KEY, userId)
            userProfileIntent.putExtra(UserProfileFragment.TAB_POS, tab)
            context.startActivity(userProfileIntent)
        }
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerBinding = ContainerWithSwipeablePlayerBinding.inflate(layoutInflater)
        binding =
            ActivityProfileBinding.inflate(layoutInflater, playerBinding.contentContainer, true)
        setContentView(playerBinding.root)

        // val bundle = intent?.extras
//        uiUser = bundle?.get("user") as UIUser?

        /*bundle?.let{
            binding.toolbar.tvToolbarTitle.text = it.getString(UserProfileFragment.USER_NAME_KEY)
        }*/

        /* binding.toolbar.btnClose.setOnClickListener {
             finish()
         }*/


        setupNavigationController()
        activityPlayerViewManager =
            ActivityPlayerViewManager(supportFragmentManager, playerBinding, playerBinder)
    }

    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_profile)
    }

    override fun isPlayerVisible() = activityPlayerViewManager?.isPlayerVisible() ?: false

    override fun showPlayer(args: PlayerViewManager.PlayerArgs, onTransitioned: (() -> Unit)?) {
        Timber.d("Clicked opening ${activityPlayerViewManager}")
        activityPlayerViewManager?.showPlayer(args, onTransitioned)
    }

    override fun hidePlayer() {
        activityPlayerViewManager?.hidePlayer()
    }

    override fun navigateToHashTag(hashtag: TagUIModel) {
        navController.popBackStack(R.id.profile_fragment, false)
        navController.navigate(
            R.id.action_another_profile_fragment_to_discoverHashtagFragment,
            bundleOf(
                DiscoverHashtagFragment.HASHTAG_KEY to hashtag,
                DiscoverHashtagFragment.KEY_SHOW_NOTIFICATION_ICON to false
            )
        )
    }

    override fun playPreview(audio: AudioService.AudioTrack, startPosition: Int, endPosition: Int) {
        activityPlayerViewManager?.playPreview(audio, startPosition, endPosition)
    }

    override fun stopPreview() {
        activityPlayerViewManager?.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}