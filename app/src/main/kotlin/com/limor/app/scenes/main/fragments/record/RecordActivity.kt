package com.limor.app.scenes.main.fragments.record


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.limor.app.R
import com.limor.app.common.BaseActivity
import com.limor.app.scenes.main.viewmodels.LocationsViewModel
import com.limor.app.scenes.utils.location.MyLocation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_record.*
import java.util.*
import javax.inject.Inject


class RecordActivity : BaseActivity(), HasSupportFragmentInjector{

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    lateinit var navController: NavController

    private lateinit var locationsViewModel: LocationsViewModel

    private val PERMISSION_ALL = 1
    private var PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    companion object {
        val TAG: String = RecordActivity::class.java.simpleName
        fun newInstance() = RecordActivity()
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = fragmentInjector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        initBottomSheetBehavior()
        window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#99000000")))
        bindViewModel()

        //Check Permissions
        if (!hasPermissions(applicationContext, *PERMISSIONS)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, PERMISSION_ALL)
            }
        }else{
            requestForLocation()
        }

        setupNavigationController()
    }

    private fun initBottomSheetBehavior() {
        val bottomSheetBehavior = BottomSheetBehavior.from(detail_container)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish()
                    overridePendingTransition(0, 0)
                }
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
    }


    private fun bindViewModel() {
        locationsViewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(LocationsViewModel::class.java)
    }


    private fun setupNavigationController() {
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment_record)
    }


    private fun requestForLocation(){
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }


        val locationResult: MyLocation.LocationResult = object : MyLocation.LocationResult() {
            override fun gotLocation(location: Location?) {
                //Got the location!
                val geoCoder = Geocoder(applicationContext, Locale.getDefault()) //it is Geocoder
                try {
                    val address: List<Address> = geoCoder.getFromLocation(
                        location!!.latitude,
                        location.longitude,
                        1
                    )
                    when {
                        address[0].locality != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].locality
                        }
                        address[0].adminArea != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].adminArea
                        }
                        address[0].thoroughfare != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].thoroughfare
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val myLocation = MyLocation()
        myLocation.getLocation(applicationContext, locationResult)
    }


    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

}