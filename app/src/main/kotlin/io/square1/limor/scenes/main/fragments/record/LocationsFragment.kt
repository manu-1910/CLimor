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
import io.square1.limor.scenes.main.fragments.record.adapters.LocationsAdapter
import io.square1.limor.scenes.main.viewmodels.LocationsViewModel
import io.square1.limor.uimodels.UILocations
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
            locationsViewModel.localListLocations.clear()

            locationSelectedItem.let {
                if (it != null) {
                    locationsViewModel.locationSelectedItem = it
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

