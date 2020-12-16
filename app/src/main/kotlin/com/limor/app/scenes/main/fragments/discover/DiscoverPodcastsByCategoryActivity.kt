package com.limor.app.scenes.main.fragments.discover

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.limor.app.R
import com.limor.app.common.BaseActivity
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class DiscoverPodcastsByCategoryActivity : BaseActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    companion object {
        val TAG: String = DiscoverPodcastsByCategoryActivity::class.java.simpleName
        fun newInstance() = DiscoverPodcastsByCategoryActivity()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcasts_by_category)


        setupNavigationController()
    }

    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_podcasts_by_category)
    }


}