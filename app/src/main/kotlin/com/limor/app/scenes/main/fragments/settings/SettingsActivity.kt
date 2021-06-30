package com.limor.app.scenes.main.fragments.settings

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.databinding.ActivitySettingsBinding
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.view.*
import javax.inject.Inject


class SettingsActivity : BaseActivity(), HasSupportFragmentInjector {


    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: ActivitySettingsBinding

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController


    val model: SettingsViewModel by viewModels { viewModelFactory }
    companion object {
        val TAG: String = SettingsActivity::class.java.simpleName
        fun newInstance() = SettingsActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0,0)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.tvToolbarTitle.text  = getString(R.string.settings)
        model.settingsToolBarTitle.observe(this,{
            binding.toolbar.tvToolbarTitle.text  = it
        })

        setupNavigationController()
    }

    private fun setupNavigationController() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigation_host_fragment_settings) as NavHostFragment
        navController = navHostFragment.navController

        binding.toolbar.btnClose.setOnClickListener {
            onBackPressed()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(0,0)
    }

    /*override fun onBackPressed() {
        val backStackEntryCount = navHostFragment.childFragmentManager.fragments.size
        if(backStackEntryCount==1){
            super.onBackPressed()
        }else{
            navController.popBackStack()
        }
    }*/
}