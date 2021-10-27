package com.limor.app.scenes.patron.setup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.limor.app.R
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import timber.log.Timber
import javax.inject.Inject


class PatronSetupActivity : AppCompatActivity(), HasSupportFragmentInjector {

    lateinit var navController: NavController
    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patron_setup)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navigation_host_patron_setup) as NavHostFragment
        navController = navHostFragment.navController
        intent?.let {
            Timber.d("NAV  ${it.getStringExtra("page")}")
            when (it.getStringExtra("page")) {
                "categories" -> {
                    val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentPatronCategories,true).build()
                    navController.navigate(R.id.fragmentPatronCategories, null,navOptions)
                }
                "languages" -> {
                    val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentPatronLanguages,true).build()
                    navController.navigate(R.id.fragmentPatronLanguages, null,navOptions)
                }
            }
        }

    }

    override fun onBackPressed() {
        if(navController.currentDestination?.id == R.id.patronPricingPlansFragment){
            super.onBackPressed()
        }
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

}