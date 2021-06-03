package com.limor.app.scenes.main_new

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.limor.app.R
import kotlinx.android.synthetic.main.activity_main_new.*
import timber.log.Timber

class MainActivityNew : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)
        clActivityMainNew.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setUpBottomNavigation()
    }

    private fun setUpBottomNavigation() {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navigation.setupWithNavController(navController)
        navigation.setOnNavigationItemSelectedListener {item ->
            Timber.d("setOnNavigationItemSelectedListener -> ${item.title} ")
            true
//           onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment))
        }
    }
}