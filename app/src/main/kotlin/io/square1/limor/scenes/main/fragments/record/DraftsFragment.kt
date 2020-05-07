package io.square1.limor.scenes.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.adapters.DraftAdapter
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.uimodels.UIDraft
import kotlinx.android.synthetic.main.toolbar_default.btnToolbarRight
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_cross.*
import org.jetbrains.anko.sdk23.listeners.onClick
import timber.log.Timber
import javax.inject.Inject


class DraftsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var draftViewModel: DraftViewModel

    private var rootView: View? = null
    private var fabAddDraft: FloatingActionButton? = null
    private var rlytDraftsPlaceHolder: RelativeLayout? = null
    private var rvCentres: RecyclerView? = null
    private var adapter: DraftAdapter? = null
    private var pbMainDraft: ProgressBar? = null
    private var existDraftFlag = false
    private var listDrafts = ArrayList<UIDraft>()
    private var listRealmDrafts = ArrayList<UIDraft>()

    private val deleteDraftTrigger = PublishSubject.create<Unit>()
    private val loadDraftsTrigger = PublishSubject.create<Unit>()

    var app: App? = null

    companion object {
        val TAG: String = DraftsFragment::class.java.simpleName
        fun newInstance() = DraftsFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_drafts, container, false)

            fabAddDraft = rootView?.findViewById(R.id.fabAddCentre)
            rlytDraftsPlaceHolder = rootView?.findViewById(R.id.rlytCentersPlaceHolder)
            rvCentres = rootView?.findViewById(R.id.rvCentres)
            pbMainDraft = rootView?.findViewById(R.id.pbMainCentre)
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
        configureAdapter()

    }



    private fun bindViewModel() {
        /* activity?.let { fragmentActivity ->
             mainViewModel = ViewModelProviders
                 .of(fragmentActivity, viewModelFactory)
                 .get(MainViewModel::class.java)
         }*/

        draftViewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(DraftViewModel::class.java)
    }


    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvCentres?.layoutManager = layoutManager
        adapter = DraftAdapter(
            listDrafts,
            object : DraftAdapter.OnItemClickListener {
                override fun onItemClick(item: UIDraft, position: Int) {
                    //if (item.isDraft!!) {
                    //    val intent = Intent(context, AddCentreMainActivity::class.java)
                    //    existDraftFlag = false
                    //    val bundle = Bundle()
                    //    bundle.putSerializable(
                    //        getString(R.string.centre_key),
                    //        draftViewModel.uiCentre
                    //    )
                    //    intent.putExtra(getString(R.string.centre_bundle_key), bundle)
                    //    startActivity(intent)
                    //} else {
                    //    val intent = Intent(context, CentreDetailsActivity::class.java)
                    //    val bundle = Bundle()
                    //    bundle.putSerializable(getString(R.string.centre_key), item)
                    //    intent.putExtra(getString(R.string.centre_bundle_key), bundle)
                    //    startActivity(intent)
                    //}
                }
            },
            object : DraftAdapter.OnSecondaryInformationListenerClickListener {
                override fun onSecondaryInformationClick(item: UIDraft, position: Int) {
                    //val intent = Intent(context, AddCentreMainActivity::class.java)
                    //existDraftFlag = false
                    //val bundle = Bundle()
                    //bundle.putSerializable(getString(R.string.centre_key), draftViewModel.uiCentre)
                    //intent.putExtra(getString(R.string.centre_bundle_key), bundle)
                    //startActivity(intent)
                }
            }
        )
        rvCentres?.adapter = adapter
        rvCentres?.setHasFixedSize(false)
    }


    private fun listeners() {
        fabAddDraft?.onClick {
           //val intent = Intent(context, AddCentreMainActivity::class.java)
           //if (existDraftFlag) {
           //    alert(getString(R.string.half_centre), getString(R.string.centre_draft)) {
           //        positiveButton(getString(R.string.edit)) {
           //            existDraftFlag = false
           //            val bundle = Bundle()
           //            bundle.putSerializable(
           //                getString(R.string.centre_key),
           //                draftViewModel.uiCentre
           //            )
           //            intent.putExtra(getString(R.string.centre_bundle_key), bundle)
           //            startActivity(intent)
           //        }
           //        negativeButton(getString(R.string.delete)) {
           //            existDraftFlag = false
           //            deleteDraftTrigger.onNext(Unit)
           //            startActivity(intent)
           //        }
           //        neutralPressed(getString(R.string.cancel)) {}
           //    }.show()

           //} else {
           //    existDraftFlag = false
           //    startActivity(intent)
           //}
        }
    }

    private fun loadDraftCentre() {
        //Observer of LiveData ViewModel
        draftViewModel.loadDraftRealm()?.observe(this, Observer<List<UIDraft>> {
            listRealmDrafts.clear()
            it.map { uiRealmDraft ->
                //uiRealmCentre.isDraft = true
                listRealmDrafts.add(uiRealmDraft)
                draftViewModel.uiDraft = uiRealmDraft
                existDraftFlag = true
            }
        })
    }

    private fun deleteDraftCentre() {
        val output = draftViewModel.deleteDraftRealm(
            DraftViewModel.InputDelete(
                deleteDraftTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it) {
                Timber.e(getString(R.string.draft_deleted))
            } else
                Timber.e(getString(R.string.draft_not_deleted))

        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })
        output.errorMessage.observe(this, Observer {
            Timber.e(getString(R.string.centre_not_deleted_error))
        })
    }


    override fun onResume() {
        super.onResume()
        pbMainDraft?.visibility = View.VISIBLE
        loadDraftsTrigger.onNext(Unit)
    }


    private fun configureToolbar() {

        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_drafts)

        //Toolbar Left
        btnClose.onClick {
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_edit)
        btnToolbarRight.onClick {
            try {
                //Setup animation transition
                ViewCompat.setTranslationZ(view!!, 1f)

                findNavController().navigate(R.id.action_record_drafts_to_record_edit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




}

