package com.limor.app.scenes.patron.unipaas

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.scenes.auth_new.util.PrefsHandler
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import timber.log.Timber
import javax.inject.Inject

class UniPaasActivity : BaseActivity(), HasSupportFragmentInjector {

    lateinit var navController: NavController
    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uni_paas)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.uniPaasNavContainer) as NavHostFragment
        navController = navHostFragment.navController
        intent?.let {
            if (it.getBooleanExtra("show_confirmation", false)) {
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.unipaas_set_up_confirmation_fragment,true).build()
                navController.navigate(R.id.unipaas_set_up_confirmation_fragment, bundleOf("url" to PrefsHandler.getOnboardingUrl(this)), navOptions)
            }
        }

    }

}