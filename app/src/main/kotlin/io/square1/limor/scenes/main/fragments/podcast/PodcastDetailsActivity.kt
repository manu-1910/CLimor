package io.square1.limor.scenes.main.fragments.podcast

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.uimodels.UIFeedItem
import javax.inject.Inject


class PodcastDetailsActivity : BaseActivity(), HasSupportFragmentInjector {

    var uiFeedItem : UIFeedItem? = null

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    companion object {
        val TAG: String = PodcastDetailsActivity::class.java.simpleName
        fun newInstance() = PodcastDetailsActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_details)

        val bundle = intent?.extras
        uiFeedItem = bundle?.get("model") as UIFeedItem?

        setupNavigationController()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigation_host_fragment_podcast_details)
        //val fragmentList = navHostFragment?.childFragmentManager?.fragments
        //var cosa = fragmentList?.get(0)
    }




    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_podcast_details)
    }

}