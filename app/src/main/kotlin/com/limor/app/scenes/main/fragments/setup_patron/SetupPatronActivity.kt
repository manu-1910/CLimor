package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.uimodels.UIUser
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.sdk23.listeners.onClick
import javax.inject.Inject

class SetupPatronActivity : BaseActivity(), HasSupportFragmentInjector {

    var uiUser: UIUser? = null


    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    companion object {
        val TAG: String = SetupPatronActivity::class.java.simpleName
        fun newInstance() = SetupPatronActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_patron)

        val bundle = intent?.extras
        uiUser = bundle?.get("user") as UIUser?

        setupNavigationController()
        listeners()
    }

    private fun listeners() {
        btnClose?.onClick {
            onBackPressed()
        }
    }

    private fun setupNavigationController() {
        navController =
            Navigation.findNavController(this, R.id.navigation_host_fragment_setup_patron)
    }

}