package io.square1.limor.scenes.main.fragments.record


import android.os.Bundle
import android.os.Environment
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
import io.square1.limor.scenes.main.fragments.record.adapters.DraftAdapter
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.scenes.utils.CommonsKt.Companion.copyFile
import io.square1.limor.scenes.utils.CommonsKt.Companion.getDateTimeFormatted
import io.square1.limor.uimodels.UIDraft
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import java.io.File
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
    private val insertDraftsTrigger = PublishSubject.create<Unit>()
    private var draftsLocalList: ArrayList<UIDraft> = ArrayList()
    private var adapter: DraftAdapter? = null
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
            insertDraft()
        }
        configureToolbar()
        app = context?.applicationContext as App
        pbDrafts?.visibility = View.VISIBLE
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 20f)
    }


    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)
        }
    }


    private fun configureToolbar() {

        //Toolbar title
        tvTitleToolbar?.text = getString(R.string.title_drafts)

        //Toolbar Left
        btnCloseToolbar?.let {
            it.onClick {
                try {
                    if(adapter?.mediaPlayer!!.isPlaying){
                        adapter?.mediaPlayer!!.stop()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                findNavController().popBackStack()
            }
        }

        //Toolbar Right
        btnEditToolbarUpdate?.visibility = View.VISIBLE
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


            try {
                if(adapter?.mediaPlayer!!.isPlaying){
                    adapter?.mediaPlayer!!.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            rvDrafts?.adapter?.notifyDataSetChanged()
        }

    }


    private fun configureAdapter() {
        val layoutManager = LinearLayoutManager(context)
        rvDrafts?.layoutManager = layoutManager
        adapter = context?.let {
            DraftAdapter(
                it,
                draftsLocalList,
                object : DraftAdapter.OnItemClickListener {
                    override fun onItemClick(item: UIDraft) {
                        if (!comesFromEditMode) { }
                    }
                },
                object : DraftAdapter.OnDeleteItemClickListener {
                    override fun onDeleteItemClick(position: Int) {
                        pbDrafts?.visibility = View.VISIBLE

                        try {
                            if(adapter?.mediaPlayer!!.isPlaying){
                                adapter?.mediaPlayer!!.stop()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        draftViewModel.uiDraft = draftsLocalList[position]
                        deleteDraftsTrigger.onNext(Unit)

                        //Remove the audio file from the folder
                        try {
                            val file = File(draftsLocalList[position].filePath)
                            file.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        //Remove item from the list
                        draftsLocalList.removeAt(position)

                        rvDrafts?.adapter?.notifyItemRemoved(position)
                        //rvDrafts?.adapter?.notifyItemRangeChanged(0, draftsLocalList.size)


                    }
                },
                object : DraftAdapter.OnDuplicateItemClickListener {
                    override fun onDuplicateItemClick(position: Int) {
                        pbDrafts?.visibility = View.VISIBLE

                        try {
                            if(adapter?.mediaPlayer!!.isPlaying){
                                adapter?.mediaPlayer!!.stop()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        var newItem = draftsLocalList[position]
                        newItem.id = System.currentTimeMillis()
                        if(draftsLocalList[position].title.toString().isNotEmpty()){
                            newItem.title = getString(R.string.copy_of) + newItem.title
                        }else{
                            newItem.title = getString(R.string.duplicated_draft)
                        }
                        newItem.date = getDateTimeFormatted()

                        val originalFile = File(draftsLocalList[position].filePath)
                        val destFile = File(context?.getExternalFilesDir(null)?.absolutePath + "/limorv2/" + System.currentTimeMillis() + CommonsKt.audioFileFormat)
                        try {
                            copyFile(originalFile, destFile)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        newItem.filePath = destFile.absolutePath

                        //Insert in Realm
                        draftViewModel.uiDraft = newItem
                        insertDraftsTrigger.onNext(Unit)

                        //Add item to the list
                        draftsLocalList.add(newItem)
                        rvDrafts?.adapter?.notifyItemChanged(position)
                    }
                },
                object : DraftAdapter.OnEditItemClickListener {
                    override fun onEditItemClick(item: UIDraft) {
                        pbDrafts?.visibility = View.VISIBLE

                        try {
                            if(adapter?.mediaPlayer!!.isPlaying){
                                adapter?.mediaPlayer!!.stop()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

//                        //Go to record fragment to continue recording
//                        draftViewModel.uiDraft = item
//                        draftViewModel.filesArray.add(File(item.filePath))
//                        draftViewModel.continueRecording = true
//                        draftViewModel.durationOfLastAudio = item.length!!
//
//                        try {
//                            //navController.navigate(R.id.action_record_drafts_to_record_fragment)
//
//                            findNavController().popBackStack()
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }

                        //Go to record fragment to continue recording
                        draftViewModel.uiDraft = item
                        draftViewModel.filesArray.add(File(item.filePath))
                        draftViewModel.continueRecording = true
                        //draftViewModel.durationOfLastAudio = item.length!!

                        val bundle = bundleOf("recordingItem" to draftViewModel.uiDraft)
                        findNavController().navigate(R.id.action_record_drafts_to_record_edit, bundle)
                    }
                }
//                ,
//                object : DraftAdapter.OnContinueRecordingItemClickListener {
//                    override fun onContinueRecordingItemClick(item: UIDraft, position: Int) {
//
//                        draftViewModel.uiDraft = item
//                        draftViewModel.filesArray.add(File(item.filePath))
//                        draftViewModel.continueRecording = true
//
//                        try {
//                            findNavController().popBackStack()
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
////**************************************************************************************************
////                        pbDrafts?.visibility = View.VISIBLE
////
////                        var newItem = draftsLocalList[position]
////                        newItem.id = System.currentTimeMillis()
////                        newItem.title = getString(R.string.duplicated_draft)
////                        newItem.caption = getDateTimeFormatted()
////
////                        try {
////                            val originalFile = File(draftsLocalList[position].filePath)
////                            val destFile = File(context?.getExternalFilesDir(null)?.absolutePath + "/limorv2/" + System.currentTimeMillis() +".amr")
////                            copyFile(originalFile, destFile)
////                        } catch (e: Exception) {
////                            e.printStackTrace()
////                        }
////
////                        //Insert in Realm
////                        draftViewModel.uiDraft = newItem
////                        insertDraftsTrigger.onNext(Unit)
////
////                        //Add item to the list
////                        draftsLocalList.add(newItem)
////                        rvDrafts?.adapter?.notifyItemChanged(position)
//                    }
//               },
                , findNavController()
            )
        }
        rvDrafts?.adapter = adapter
        rvDrafts?.setHasFixedSize(false)
    }


    private fun showEmptyScenario() {
        //Show empty scenario layout
        emptyScenarioDraftsLayout?.visibility = View.VISIBLE
        //Hide Recyclerview
        rvDrafts?.visibility = View.GONE
        //Hide progress bar
        pbDrafts?.visibility = View.GONE

        //Clear right toolbar button
        btnEditToolbarUpdate?.background = null
        btnEditToolbarUpdate?.text = ""
        btnEditToolbarUpdate?.onClick {null}
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
            toast(getString(R.string.centre_not_deleted_error))
        })
    }


    private fun insertDraft() {
        val output = draftViewModel.insertDraftRealm(
            DraftViewModel.InputInsert(
                insertDraftsTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it) {
                toast(getString(R.string.draft_inserted))
            } else{
                toast(getString(R.string.draft_not_inserted))
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.draft_not_inserted_error))
        })
    }


    override fun onResume() {
        super.onResume()
        pbDrafts?.visibility = View.VISIBLE
        getDraftsTrigger.onNext(Unit)
    }




}

