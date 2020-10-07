package io.square1.limor.scenes.authentication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import kotlinx.android.synthetic.main.activity_sign.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.toolbar
import javax.inject.Inject

class SignActivity: BaseActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)
        hideToolbar()
    }

    override fun onBackPressed() {
        val navHostFragment = authMainContainer as NavHostFragment
        val backStackEntryCount = navHostFragment.childFragmentManager.backStackEntryCount
        if(backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            val navController = navHostFragment.findNavController()
            navController.navigateUp()
        }
    }

    fun hideToolbar() {
        toolbar?.animate()?.alpha(0f)?.setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                btnClose.isEnabled = false
            }
        })
    }

    fun showToolbar() {
        toolbar?.animate()?.alpha(1f)?.setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                btnClose.isEnabled = true
            }
        })
    }

}