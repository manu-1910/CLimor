package com.limor.app.scenes.main_new

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.limor.app.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main_new.*

class MainActivityNew : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)
        AndroidInjection.inject(this)
        clActivityMainNew.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setUpBottomNavigation()
    }

    lateinit var navController: NavController

    private fun setUpBottomNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navigation.setupWithNavController(navController)
    }
}