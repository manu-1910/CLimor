package io.square1.limor.scenes.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.scenes.main.viewmodels.PublishViewModel
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import kotlinx.android.synthetic.main.toolbar_with_searchview.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import javax.inject.Inject


class LocationsFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private lateinit var publishViewModel: PublishViewModel


    private var rootView: View? = null
    var app: App? = null




    companion object {
        val TAG: String = LocationsFragment::class.java.simpleName
        fun newInstance() = LocationsFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_locations, container, false)

            /*audioSeekbar = rootView?.findViewById(R.id.sbProgress)
            timePass = rootView?.findViewById(R.id.tvTimePass)
            timeDuration = rootView?.findViewById(R.id.tvDuration)
            btnPlayPause = rootView?.findViewById(R.id.ibPlayPause)*/
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
        listeners()

    }




    private fun bindViewModel() {
//        activity?.let {
//            draftViewModel = ViewModelProviders
//                .of(it, viewModelFactory)
//                .get(DraftViewModel::class.java)
//
//            publishViewModel = ViewModelProviders
//                .of(it, viewModelFactory)
//                .get(PublishViewModel::class.java)
//        }
    }



    private fun configureToolbar() {

        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_hashtags)

        //Toolbar Left
        btnClose.onClick {
            findNavController().popBackStack()
        }

        //Toolbar Right
        btnDone.onClick {
            toast("Done clicked")
        }

        //Search View
        search_view.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                Timber.d("Text changed:$newText")
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                Timber.d("Submit:$query")
                toast("Submit:$query")
                return false
            }

        })

    }


    private fun listeners(){
//        lytImagePlaceholder?.onClick {
//            loadImagePicker()
//        }
//        draftImage?.onClick {
//            loadImagePicker()
//        }
//
//        btnSaveDraft?.onClick {
//            addDataToRecordingItem()
//            findNavController().navigate(R.id.action_record_publish_to_record_drafts)
//        }
//
//        btnPublishDraft?.onClick {
//            publishPodcast()
//        }
    }



}

