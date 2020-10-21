package com.limor.app.scenes.main.fragments.record

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.main.fragments.record.adapters.LocationsAdapter
import com.limor.app.scenes.main.viewmodels.LocationsViewModel
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.utils.location.MyLocation
import com.limor.app.uimodels.UILocations
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import kotlinx.android.synthetic.main.toolbar_with_searchview.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class LocationsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var publishViewModel: PublishViewModel

    private var rvLocations: RecyclerView? = null
    private var rootView: View? = null
    private var listLocations = ArrayList<UILocations>()
    private val locationsTrigger = PublishSubject.create<Unit>()
    private var locationSelectedItem: UILocations? = null

    var app: App? = null



    companion object {
        val TAG: String = LocationsFragment::class.java.simpleName
        fun newInstance() = LocationsFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_locations, container, false)

            rvLocations = rootView?.findViewById(R.id.rvLocations)
        }
        app = context?.applicationContext as App
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        bindViewModel()
        configureToolbar()
        apiCallSearchLocations()
        setupRecycler(listLocations)

        if(locationsViewModel.uiLocationsRequest.term.isNotEmpty()){
            locationsTrigger.onNext(Unit)
        }
    }


    private fun bindViewModel() {
        activity?.let {
            locationsViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(LocationsViewModel::class.java)

            publishViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(PublishViewModel::class.java)
        }
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_location)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnDone.onClick {
            locationsViewModel.localListLocations.clear()

            locationSelectedItem.let {
                if (it != null) {
                    locationsViewModel.locationSelectedItem = it
                    publishViewModel.locationSelectedItem = it
                }
            }
            findNavController().popBackStack()
        }



        //Search View
        search_view.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty() && newText.length > 3){
                    searchLocations(newText)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty() && query.length > 3){
                    searchLocations(query)
                }else{
                    toast(getString(R.string.min_3_chars))
                }
                return false
            }
        })
    }


    private fun searchLocations(term: String){
        //Make api call
        if (!term.isNullOrEmpty()){
            locationsViewModel.uiLocationsRequest.term = term
            locationsTrigger.onNext(Unit)
        }
    }



    private fun callForLocationUpdates(){
        val locationResult: MyLocation.LocationResult = object : MyLocation.LocationResult() {
            override fun gotLocation(location: Location?) {
                //Got the location!
                val geoCoder = Geocoder(context, Locale.getDefault()) //it is Geocoder
                try {
                    val address: List<Address> = geoCoder.getFromLocation(
                        location!!.latitude,
                        location.longitude,
                        1
                    )
                    when {
                        address[0].locality != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].locality
                            locationsTrigger.onNext(Unit)
                        }
                        address[0].adminArea != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].adminArea
                            locationsTrigger.onNext(Unit)
                        }
                        address[0].thoroughfare != null -> {
                            locationsViewModel.uiLocationsRequest.term = address[0].thoroughfare
                            locationsTrigger.onNext(Unit)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val myLocation = MyLocation()
        myLocation.getLocation(context!!, locationResult)
    }


    private fun apiCallSearchLocations() {
        val output = locationsViewModel.transform(
            LocationsViewModel.Input(
                locationsTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) { //Tags Response Ok
                if (it.data.locations.size > 0) {
                    //hidePlaceHolder()
                    listLocations.clear()
                    listLocations.addAll(it.data.locations)
                    setupRecycler(listLocations)
                    rvLocations?.adapter?.notifyDataSetChanged()
                } else {
                    //showPlaceHolder()
                }
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()
            if (app!!.merlinsBeard!!.isConnected) {

                val message: StringBuilder = StringBuilder()
                if (it.errorMessage!!.isNotEmpty()) {
                    message.append(it.errorMessage)
                } else {
                    message.append(R.string.some_error)
                }

                if(it.code == 10){  //Session expired
                    alert(message.toString()) {
                        okButton {
                            val intent = Intent(context, SignActivity::class.java)
                            //intent.putExtra(getString(R.string.otherActivityKey), true)
                            startActivityForResult(
                                intent,
                                resources.getInteger(R.integer.REQUEST_CODE_LOGIN_FROM_PUBLISH)
                            )
                        }
                    }.show()
                }else{
                    alert(message.toString()) {
                        okButton { }
                    }.show()
                }

            } else {
                alert(getString(R.string.default_no_internet)) {
                    okButton {}
                }.show()
            }
        })
    }


    private fun setupRecycler(tagList: ArrayList<UILocations>) {
        rvLocations?.layoutManager = LinearLayoutManager(context)
        rvLocations?.adapter = LocationsAdapter(
            listLocations,
            object : LocationsAdapter.OnItemClickListener {
                override fun onItemClick(item: UILocations) {

                    locationSelectedItem?.isSelected = false //Des-select previous selected item
                    locationSelectedItem = item

                    for (location in listLocations) {
                        if (item.address == location.address) {
                            listLocations[listLocations.indexOf(item)].isSelected = !listLocations[listLocations.indexOf(item)].isSelected
                            break
                        }
                    }

                    rvLocations?.adapter?.notifyDataSetChanged()
                }
            })
    }


}

