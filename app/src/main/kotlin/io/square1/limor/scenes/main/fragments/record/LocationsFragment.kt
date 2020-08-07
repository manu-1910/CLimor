package io.square1.limor.scenes.main.fragments.record

import android.content.Intent
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
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.fragments.record.adapters.HashtagsAdapter
import io.square1.limor.scenes.main.fragments.record.adapters.LocationsAdapter
import io.square1.limor.scenes.main.viewmodels.LocationsViewModel
import io.square1.limor.uimodels.UILocations
import io.square1.limor.uimodels.UITags
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import kotlinx.android.synthetic.main.toolbar_with_searchview.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class LocationsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var locationsViewModel: LocationsViewModel

    private var rvLocations: RecyclerView? = null
    private var rootView: View? = null
    private var adapter: LocationsAdapter? = null
    private var listLocations = ArrayList<UILocations>()
    private var listLocationsSelected = ArrayList<UILocations>()
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
        //configureAdapter()
        apiCallSearchTags()
        setupRecycler(listLocations)
    }


    private fun bindViewModel() {
        activity?.let {
            locationsViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(LocationsViewModel::class.java)
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
            toast("Done clicked")
//            var test = listLocationsSelected
//            println("selected tags are: " +listLocationsSelected)

            //viewModelSearchProperties.uiSearchRequest.localityArraySelectedItems.clear()
            //viewModelSearchProperties.uiSearchRequest.localityArray.clear()

            locationsViewModel.localListLocationsSelected.clear()
            locationsViewModel.localListLocations.clear()

            locationsViewModel.locationSelectedItem = locationSelectedItem!!
            //locationsViewModel.localListLocations.addAll(listLocations)
            //locationsViewModel.localListLocationsSelected.addAll(listLocationsSelected)

            findNavController().popBackStack()

        }



        //Search View
        search_view.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                //Timber.d("Text changed:$newText")
                if (newText.isNotEmpty() && newText.length > 3){
                    println("entro aquí onquerytextchange")
                    //searchTags(newText)
                    //setupRecycler(listLocations)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                //Timber.d("Submit:$query")
                if (query.isNotEmpty() && query.length > 3){
                    searchTags(query)
                }else{
                    toast("You should type at least 3 characters")
                }
                return false
            }

        })
    }


    private fun searchTags(term: String){
        //Make api call
        locationsViewModel.uiLocationsRequest.term = term
        locationsTrigger.onNext(Unit)
    }


    private fun apiCallSearchTags() {
        val output = locationsViewModel.transform(
            LocationsViewModel.Input(
                locationsTrigger
            )
        )

        output.response.observe(this, Observer {
            pbSignUp?.visibility = View.GONE
            view?.hideKeyboard()

            if (it.code == 0) { //Tags Response Ok

                listLocations.clear()
                //listLocationsSelected.clear()
                //listLocationsSelected.addAll(locationsViewModel.localListLocationsSelected)

                //Add others localities
                for (location in it.data.locations) {
                    listLocations.add(location)
                }

//                var someLocationSelected = false
//
//                //Check if localityArrayItemsSelected is empty or not
//                if (listLocationsSelected.isNotEmpty()) {
//                    for (location in listLocations) {
//                        //Set selection
//                        for (tagSelected in listLocationsSelected) {
//                            if (location.address == tagSelected.address) {
//                                location.isSelected = true
//                                someLocationSelected = true
//                            }
//                        }
//                    }
//                }
//                if (someLocationSelected){
//                    //Set any isSelected false
//                    for (location in listLocations){
//                        if (location.address == ""){
//                            location.isSelected = false
//                        }
//                    }
//                }

            }

            //adapter?.notifyDataSetChanged()


            rvLocations?.adapter?.notifyDataSetChanged()



//                if (it.data.tags.size > 0) {
//                    hidePlaceHolder()
//                    listLocations.addAll(it.data.tags)
//                    rvLocations?.adapter?.notifyDataSetChanged()
//                } else {
//                    showPlaceHolder()
//                }


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

                    locationSelectedItem = item

                    for (location in listLocations) {
                        if (item.address == location.address) {
                            listLocations[listLocations.indexOf(item)].isSelected = !listLocations[listLocations.indexOf(item)].isSelected
                            break
                        }
                    }

//                    //If no item selected -> select any
//                    var itemSelected = false
//                    for (location in listLocations) {
//                        if (location.isSelected){
//                            itemSelected = true
//                        }
//                    }
//
//                    //Add tags to tagsSelectedArray
//                    listLocationsSelected.clear()
//                    for (location in listLocations) {
//                        if (location.isSelected)
//                            listLocationsSelected.add(location)
//                    }

                    rvLocations?.adapter?.notifyDataSetChanged()
                }
            })
    }





//    private fun configureAdapter() {
//        val layoutManager = LinearLayoutManager(context)
//        rvLocations?.layoutManager = layoutManager
//        adapter = HashtagsAdapter(
//            listLocations,
//            object : HashtagsAdapter.OnItemClickListener {
//                override fun onItemClick(item: UITags, position: Int) {
////                    val intent = Intent(context, OfficeDetailsActivity::class.java)
//////                    val bundle = Bundle()
//////                    bundle.putSerializable(getString(R.string.centre_key), viewModel.uiCentre)
//////                    bundle.putSerializable(getString(R.string.office_key), item)
//////                    intent.putExtra(getString(R.string.office_bundle_key), bundle)
//////                    startActivity(intent)
//                    toast("item clicked: " + position)
//                }
//            }
//        )
//        rvLocations?.adapter = adapter
//        rvLocations?.setHasFixedSize(false)
//    }


}

