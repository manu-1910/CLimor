package com.limor.app.scenes.main.fragments.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.limor.app.R
import com.limor.app.common.ViewModelFactory
import com.limor.app.databinding.ActivitySettingsBinding
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class OpenSettings : ActivityResultContract<Void?, Boolean>() {

    override fun createIntent(context: Context, input: Void?) =
        Intent(context, SettingsActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?) =
        resultCode == SettingsActivity.SETTINGS_RESULT_CHANGED
}

class SettingsActivity : AppCompatActivity(), HasSupportFragmentInjector {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: ActivitySettingsBinding

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    val model: SettingsViewModel by viewModels { viewModelFactory }

    companion object {
        val TAG: String = SettingsActivity::class.java.simpleName
        const val SETTINGS_RESULT_CHANGED = 12_000
        const val SETTINGS_RESULT_UNCHANGED = 12_001
        fun newInstance() = SettingsActivity()
        fun finishWithResult(targetActivity: Activity?, hasChanges: Boolean) {
            val activity = targetActivity ?: return

            // Set the result to a known value, in a future iteration this should tell the caller
            // of this activity whether the User settings/profile has changed or not.
            activity.apply {
                setResult(if (hasChanges) SETTINGS_RESULT_CHANGED else SETTINGS_RESULT_UNCHANGED)
                finish()
            }
        }
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

    override fun onBackPressed() {
        val queueSize = navHostFragment.navController.backQueue.size

        // The queueSize is 2 when the user is in the main screen/fragment,
        // i.e. the SettingsFragment, 2 because before that there's the settings_nav destination,
        // i.e. the root navigation. This means that when the size of the queue is 2 the user is
        // exiting the Settings activity.

        if (queueSize == 2) {
            finishWithResult(this, false)
        } else {
            super.onBackPressed()
        }
    }
}