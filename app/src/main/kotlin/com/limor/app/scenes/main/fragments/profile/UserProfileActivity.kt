package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.facebook.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.databinding.ActivityProfileBinding
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.scenes.utils.ActivityPlayerViewManager
import com.limor.app.scenes.utils.PlayerViewManager
import com.limor.app.service.PlayerBinder
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

    override fun showPlayer(args: PlayerViewManager.PlayerArgs) {
        Timber.d("Clicked opening ${activityPlayerViewManager}")
        activityPlayerViewManager?.showPlayer(args)
    }

    override fun hidePlayer() {
        activityPlayerViewManager?.hidePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerBinder.stop()
        activityPlayerViewManager?.hidePlayer()
    }

}