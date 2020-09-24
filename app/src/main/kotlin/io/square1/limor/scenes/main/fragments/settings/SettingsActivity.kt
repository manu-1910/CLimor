package io.square1.limor.scenes.main.fragments.settings


import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.R
import io.square1.limor.common.BaseActivity
import io.square1.limor.scenes.main.viewmodels.LocationsViewModel
import javax.inject.Inject


class SettingsActivity : BaseActivity(), HasSupportFragmentInjector{

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    private lateinit var locationsViewModel: LocationsViewModel

    private val PERMISSION_ALL = 1
    private var PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    companion object {
        val TAG: String = SettingsActivity::class.java.simpleName
        fun newInstance() = SettingsActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //bindViewModel()



        //setupNavigationController()
    }


//    private fun bindViewModel() {
//        locationsViewModel = ViewModelProviders
//            .of(this, viewModelFactory)
//            .get(LocationsViewModel::class.java)
//    }
//
//
//    private fun setupNavigationController() {
//        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_record)
//    }





}