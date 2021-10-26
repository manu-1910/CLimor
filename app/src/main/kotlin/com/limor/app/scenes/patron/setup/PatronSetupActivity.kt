package com.limor.app.scenes.patron.setup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.limor.app.R
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class PatronSetupActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patron_setup)
        val navController: NavController =
            Navigation.findNavController(this, R.id.navigation_host_patron_setup)
        intent?.extras?.let {
            when (it.getString("page")) {
                "pay" -> navController.navigate(R.id.patronPricingPlansFragment)
                "categories" -> navController.navigate(R.id.fragmentPatronCategories)
                "languages" -> navController.navigate(R.id.fragmentPatronLanguages)
                else -> navController.navigate(R.id.patronPricingPlansFragment)
            }
        }

    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector

}