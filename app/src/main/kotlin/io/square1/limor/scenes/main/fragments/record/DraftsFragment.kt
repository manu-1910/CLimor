package io.square1.limor.scenes.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
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
import io.square1.limor.scenes.main.adapters.testJavaAdapter


import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.uimodels.UIDraft
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import javax.inject.Inject


class DraftsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel

    private var rootView: View? = null
    private var rvDrafts: RecyclerView? = null
    private var pbDrafts: ProgressBar? = null
    private var emptyScenarioDraftsLayout: View? = null
    private val getDraftsTrigger = PublishSubject.create<Unit>()
    private val deleteDraftsTrigger = PublishSubject.create<Unit>()
    private var draftsLocalList: ArrayList<UIDraft> = ArrayList()
    //private var adapter: DraftAdapter? = null
    private var adapter: testJavaAdapter? = null
    private var comesFromEditMode = false

    private var btnEditToolbarUpdate: Button? = null
    private var btnCloseToolbar: ImageButton? = null
    private var tvTitleToolbar: TextView? = null

    var app: App? = null

    companion object {
        val TAG: String = DraftsFragment::class.java.simpleName
        fun newInstance(bundle: Bundle? = null): DraftsFragment {
            val fragment = DraftsFragment()
            bundle?.let { fragment.arguments = it }
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_drafts, container, false)
            rvDrafts = rootView?.findViewById<RecyclerView>(R.id.rvDrafts)
            pbDrafts = rootView?.findViewById(R.id.pbDrafts)
            emptyScenarioDraftsLayout = rootView?.findViewById(R.id.emptyScenarioDraftsLayout)

            btnEditToolbarUpdate = rootView?.findViewById(R.id.btnToolbarRight)
            btnCloseToolbar = rootView?.findViewById(R.id.btnClose)
            tvTitleToolbar = rootView?.findViewById(R.id.tvToolbarTitle)

            bindViewModel()
            configureAdapter()
            loadDrafts()
            deleteDraft()
        }
        configureToolbar()
        app = context?.applicationContext as App
        pbDrafts?.visibility = View.VISIBLE
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)
    }


    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)
        }
    }


    private fun configureToolbar() {

        //btnEditToolbarUpdate = (activity as RecordActivity).findViewById(R.id.btnToolbarRight)
        //btnCloseToolbar = (activity as RecordActivity).findViewById(R.id.btnClose)
        //tvTitleToolbar = (activity as RecordActivity).findViewById(R.id.tvToolbarTitle)

        //Toolbar title
        tvTitleToolbar?.text = getString(R.string.title_drafts)

        //Toolbar Left
        btnCloseToolbar?.let {
            it.onClick {
                try {
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        //Toolbar Right
        btnEditToolbarUpdate?.text = getText(R.string.edit)
        btnEditToolbarUpdate?.onClick {

            //Setup animation transition
            ViewCompat.setTranslationZ(view!!, 1f)

            if (comesFromEditMode) {
                comesFromEditMode = false
                btnEditToolbarUpdate?.text = getString(R.string.edit)
                //Done pressed, save list
                for (draft in draftsLocalList) {
                    draft.isEditMode = false
                }

            } else {
                comesFromEditMode = true
                btnEditToolbarUpdate?.text = getString(R.string.btnDone)
                //Edit pressed, show mask
                for (draft in draftsLocalList) {
                    draft.isEditMode = true
                }
            }

            rvDrafts?.adapter?.notifyDataSetChanged()
        }

    }


    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvDrafts?.layoutManager = layoutManager
        adapter = context?.let {
            testJavaAdapter(
                it,
                draftsLocalList,
                object : testJavaAdapter.OnItemClickListener {
                    override fun onItemClick(item: UIDraft) {
                        if (!comesFromEditMode) {

                            //TODO JJ use this draft to go to edit fragment and edit it
                            //val searchPropertiesIntent =
                            //    Intent(context, SearchPropertiesActivity::class.java)
                            //configureUISearchRequestLocal(item)
                            //searchPropertiesIntent.putExtra(
                            //    getString(R.string.savedSearchesWithFilterObjectKey),
                            //    uiSearchRequestLocal
                            //)
                            //startActivityForResult(
                            //    searchPropertiesIntent,
                            //    context!!.resources.getInteger(R.integer.REQUEST_CODE_SEARCH_PROPERTIES)
                            //)
                        }
                    }
                },
                object : testJavaAdapter.OnDeleteItemClickListener {
                    override fun onDeleteItemClick(position: Int) {
                        pbDrafts?.visibility = View.VISIBLE

                        draftViewModel.uiDraft = draftsLocalList[position]
                        deleteDraftsTrigger.onNext(Unit)

                        draftsLocalList.removeAt(position)

                        rvDrafts?.adapter?.notifyItemRemoved(position)
                        rvDrafts?.adapter?.notifyItemRangeChanged(0, draftsLocalList.size)


                    }
                }
            )
        }
        rvDrafts?.adapter = adapter
        rvDrafts?.setHasFixedSize(false)
    }


    private fun showEmptyScenario() {
        emptyScenarioDraftsLayout?.visibility = View.VISIBLE
        rvDrafts?.visibility = View.GONE
    }


    private fun hideEmptyScenario() {
        emptyScenarioDraftsLayout?.visibility = View.GONE
        rvDrafts?.visibility = View.VISIBLE
    }


    private fun loadDrafts() {
        //Observer of LiveData ViewModel
        draftViewModel.loadDraftRealm()?.observe(this, Observer<List<UIDraft>> {
            draftsLocalList.clear()
            if (it.isNotEmpty()){
                it.map { uiRealmDraft ->
                    pbDrafts?.visibility = View.GONE
                    draftsLocalList.add(uiRealmDraft)
                }

                //Continue editing
                if(btnEditToolbarUpdate?.text!!.trim() == getString(R.string.btnDone)){
                    comesFromEditMode = true
                    btnEditToolbarUpdate?.text = getString(R.string.btnDone)
                    //Edit pressed, show mask
                    for (draft in draftsLocalList) {
                        draft.isEditMode = true
                    }
                }

                rvDrafts?.adapter?.notifyDataSetChanged()
            }else{
                showEmptyScenario()
            }
        })
    }


    private fun deleteDraft() {
        val output = draftViewModel.deleteDraftRealm(
            DraftViewModel.InputDelete(
                deleteDraftsTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it) {
                toast(getString(R.string.draft_deleted))
            } else {
                toast(getString(R.string.draft_not_deleted))
            }
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
        pbDrafts?.visibility = View.VISIBLE
        getDraftsTrigger.onNext(Unit)
    }




}

