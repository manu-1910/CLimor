package io.square1.limor.scenes.main.fragments.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import javax.inject.Inject


class SettingsActivity : BaseActivity(), HasSupportFragmentInjector {


    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    companion object {
        val TAG: String = SettingsActivity::class.java.simpleName
        fun newInstance() = SettingsActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right_enter, R.anim.slide_out_left_exit);
        setContentView(R.layout.activity_settings)

        setupNavigationController()
    }

    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_settings)
    }

}