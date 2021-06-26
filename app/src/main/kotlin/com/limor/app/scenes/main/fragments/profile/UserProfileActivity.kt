package com.limor.app.scenes.main.fragments.profile

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.databinding.ActivityProfileBinding
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class UserProfileActivity : BaseActivity(), HasSupportFragmentInjector {

//    var uiUser : UIUser? = null


    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    lateinit var binding : ActivityProfileBinding

    companion object {
        val TAG: String = UserProfileActivity::class.java.simpleName
        fun newInstance() = UserProfileActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent?.extras
//        uiUser = bundle?.get("user") as UIUser?

        bundle?.let{
            binding.toolbar.tvToolbarTitle.text = it.getString("user_name")

        }
        
        binding.toolbar.btnClose.setOnClickListener {
            finish()
        }


        setupNavigationController()
    }

    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_profile)
    }

}