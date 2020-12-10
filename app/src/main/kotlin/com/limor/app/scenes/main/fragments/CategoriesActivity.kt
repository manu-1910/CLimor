package com.limor.app.scenes.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.limor.app.R
import com.limor.app.common.BaseActivity
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class CategoriesActivity : BaseActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    companion object {
        val TAG: String = CategoriesActivity::class.java.simpleName
        fun newInstance() = CategoriesActivity()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)


        setupNavigationController()
    }

    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_categories)
    }


}