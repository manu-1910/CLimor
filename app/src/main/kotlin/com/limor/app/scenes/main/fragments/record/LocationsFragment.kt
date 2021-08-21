package com.limor.app.scenes.main.fragments.record

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.limor.app.App
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.fragments.record.adapters.LocationsAdapter
import com.limor.app.scenes.main.viewmodels.LocationsViewModel
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.main_new.view_model.LocationViewModel
import com.limor.app.scenes.utils.location.MyLocation
import com.limor.app.uimodels.UILocations
import com.limor.app.uimodels.UILocationsList
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import kotlinx.android.synthetic.main.toolbar_with_searchview.btnDone
import kotlinx.android.synthetic.main.toolbar_with_searchview_light.*
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.collections.ArrayList


class LocationsFragment : BaseFragment() {

    private val mPlacesClient: PlacesClient by lazy {
        val context = requireContext()
        Places.initialize(context, BuildConfig.GOOGLE_MAPS_KEY)
        Places.createClient(context)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var publishViewModel: PublishViewModel

    private val locationViewModel: LocationViewModel by activityViewModels()

    private var rvLocations: RecyclerView? = null
    private var rootView: View? = null
    private var listLocations = ArrayList<UILocationsList>()
    private val locationsTrigger = PublishSubject.create<Unit>()
    private var locationSelectedItem: UILocationsList? = null

    var app: App? = null

    companion object {
        val TAG: String = LocationsFragment::class.java.simpleName
        fun newInstance() = LocationsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        if (locationsViewModel.uiLocationsRequest.term.isNotEmpty()) {
            locationsTrigger.onNext(Unit)
        }
    }

    private fun bindViewModel() {
        activity?.let {
            locationsViewModel = ViewModelProvider(it, viewModelFactory)
                .get(LocationsViewModel::class.java)

            publishViewModel = ViewModelProvider(it, viewModelFactory)
                .get(PublishViewModel::class.java)
        }
    }

    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_location)

        //Toolbar Left
        btnClose.onClick {
            view?.hideKeyboard()
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnDone.onClick {
            locationsViewModel.localListLocations.clear()

            locationSelectedItem.let {
                if (it != null) {
                    //  locationsViewModel.locationSelectedItem.address = it.mainText
                }
            }
            // findNavController().popBackStack()
        }
        //Search View
        autoCompleteEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(newText: CharSequence, p1: Int, p2: Int, p3: Int) {
                if (newText.isNotEmpty() && newText.length > 3) {
                    // searchLocations(newText.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        val placeAdapter = PlaceArrayAdapter(
            requireContext(),
            R.layout.locations_item,
            mPlacesClient
        ) { location ->
            locationViewModel.setLocation(location)
            view?.hideKeyboard()
            findNavController().popBackStack()
        }
        autoCompleteEditText.setAdapter(placeAdapter)


        /*autoCompleteEditText.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty() && newText.length > 3) {
                    searchLocations(newText)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty() && query.length > 3) {
                    searchLocations(query)
                } else {
                    toast(getString(R.string.min_3_chars))
                }
                return false
            }
        })*/
    }


    private fun searchLocations(term: String) {
        if (!term.isNullOrEmpty()) {

        }
    }


    private fun callForLocationUpdates() {
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
        myLocation.getLocation(requireContext(), locationResult)
    }


    private fun apiCallSearchLocations() {


        /* val output = locationsViewModel.transform(
             LocationsViewModel.Input(
                 locationsTrigger
             )
         )

         output.response.observe(viewLifecycleOwner, Observer {
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

         output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
             trackBackgroudProgress(it)
         })

         output.errorMessage.observe(this, Observer {
             pbSignUp?.visibility = View.GONE
             view?.hideKeyboard()
             CommonsKt.handleOnApiError(app!!, requireContext(), this, it)
         })*/
    }


    private fun setupRecycler(tagList: ArrayList<UILocationsList>) {
        rvLocations?.layoutManager = LinearLayoutManager(context)
        rvLocations?.adapter = LocationsAdapter(
            listLocations,
            object : LocationsAdapter.OnItemClickListener {
                override fun onItemClick(item: UILocationsList) {

                    // locationSelectedItem?.isSelected = false //Des-select previous selected item
                    locationSelectedItem = item

                    /*for (location in listLocations) {
                        if (item.address == location.address) {
                            listLocations[listLocations.indexOf(item)].isSelected =
                                !listLocations[listLocations.indexOf(
                                    item
                                )].isSelected
                            break
                        }
                    }

                    rvLocations?.adapter?.notifyDataSetChanged()*/

                    locationsViewModel.localListLocations.clear()

                    locationSelectedItem.let {

                    }
                    findNavController().popBackStack()
                }
            })
    }


}

class PlaceArrayAdapter(
    context: Context,
    val resource: Int,
    private val mPlacesClient: PlacesClient,
    private val onPlace: (UILocations) -> Unit
) : ArrayAdapter<UILocationsList>(context, resource), Filterable {

    private var token = AutocompleteSessionToken.newInstance()

    private var onClick: View.OnClickListener = View.OnClickListener { view ->
        val vh = view.tag as ViewHolder

        val request = FetchPlaceRequest
            .builder(vh.location.placeID, arrayListOf(Place.Field.ADDRESS, Place.Field.LAT_LNG))
            .setSessionToken(token)
            .build()

        mPlacesClient.fetchPlace(request).addOnCompleteListener { location ->
            token = AutocompleteSessionToken.newInstance()
            if (location.isSuccessful) {
                onPlace(UILocations.fromPlace(location.result.place))
            }
        }
    }

    private fun getPredictions(constraint: CharSequence): List<AutocompletePrediction> {
        val request = FindAutocompletePredictionsRequest
            .builder()
            .setTypeFilter(TypeFilter.CITIES)
            .setSessionToken(token)
            .setQuery(constraint.toString())
            .build()

        val prediction = mPlacesClient.findAutocompletePredictions(request)

        try {
            Tasks.await(prediction, 1, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }

        if (prediction.isSuccessful) {
            return prediction.result.autocompletePredictions
        }
        return listOf()
    }

    fun getLocations(constraint: CharSequence): List<UILocationsList> {
        return getPredictions(constraint).map(UILocationsList::fromPrediction)
    }

    private fun getViewHolder(position: Int, convertView: View?, parent: ViewGroup): ViewHolder {
        var view = convertView
        val viewHolder: ViewHolder
        val location = getItem(position)!!

        if (view == null) {
            view = LayoutInflater.from(context).inflate(resource, parent, false)
            viewHolder = ViewHolder(view, location)
            view.tag = viewHolder
            view.setOnClickListener(onClick)
        } else {
            viewHolder = view.tag as ViewHolder
            viewHolder.location = location
        }
        return viewHolder
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vh = getViewHolder(position, convertView, parent)
        vh.tvLocations.text = vh.location.mainText
        return vh.itemView
    }

    class ViewHolder(itemView: View, var location: UILocationsList) : RecyclerView.ViewHolder(itemView) {
        var tvLocations: TextView = itemView.findViewById(R.id.tvLocation)
        val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()

                if (results == null || results.count == 0) {
                    return
                }

                @Suppress("UNCHECKED_CAST")
                addAll(results.values as List<UILocationsList>)
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults? {
                if (constraint.isNullOrEmpty()) {
                    return null
                }

                val results = getLocations(constraint)
                return FilterResults().apply {
                    values = results
                    count = results.size
                }
            }
        }
    }

}