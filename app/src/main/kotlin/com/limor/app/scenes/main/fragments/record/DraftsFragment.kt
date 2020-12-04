package com.limor.app.scenes.main.fragments.record


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
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
import com.limor.app.scenes.main.fragments.record.adapters.DraftAdapter
import com.limor.app.scenes.main.viewmodels.DraftViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.CommonsKt.Companion.copyFile
import com.limor.app.scenes.utils.CommonsKt.Companion.getDateTimeFormatted
import com.limor.app.uimodels.UIDraft
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_drafts_empty_scenario.*
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
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
            rvDrafts = rootView?.findViewById(R.id.rvDrafts)
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

    private fun listeners() {
        tvRecordACast?.onClick {
            findNavController().popBackStack()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 20f)

        listeners()
    }

    override fun onStart() {
        super.onStart()
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }

    private fun onBackPressed() {
        try {
            if(adapter?.mediaPlayer!!.isPlaying){
                adapter?.mediaPlayer!!.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // we delete it because we don't want the record fragment to receive any draft because we are
        // quitting this fragment by close button, not by resuming any draft
        draftViewModel.uiDraft = null
        findNavController().popBackStack()
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
                onBackPressed()
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
                        alert(getString(R.string.confirmation_delete_draft)) {
                            okButton {
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

                                //rvDrafts?.adapter?.notifyItemRemoved(position)
                                rvDrafts?.adapter?.notifyDataSetChanged()
                                //rvDrafts?.adapter?.notifyItemRangeChanged(0, draftsLocalList.size)
                            }
                            cancelButton {  }
                        }.show()
                    }
                },
                object : DraftAdapter.OnDuplicateItemClickListener {
                    override fun onDuplicateItemClick(position: Int) {
                        val newItem = draftsLocalList[position]

                        var shouldContinue = false
                        newItem.filePath?.let {path ->
                            val f = File(path)
                            shouldContinue = f.exists()
                        }

                        if(!shouldContinue) {
                            alert(getString(R.string.error_accessing_draft_file)) {
                                okButton {  }
                            }.show()
                        } else {

                            pbDrafts?.visibility = View.VISIBLE

                            try {
                                if (adapter?.mediaPlayer!!.isPlaying) {
                                    adapter?.mediaPlayer!!.stop()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            newItem.id = System.currentTimeMillis()
                            val newTitle = getDuplicatedTitle(draftsLocalList[position].title)
                            newItem.title = newTitle


                            newItem.date = getDateTimeFormatted()

                            val originalFile = File(draftsLocalList[position].filePath)
                            val destFile =
                                File(context?.getExternalFilesDir(null)?.absolutePath + "/limorv2/" + System.currentTimeMillis() + CommonsKt.audioFileFormat)
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
                    }
                },

                    // This is deprecated, it's old code. This will never be called.
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

                        //Go to record fragment to continue recording
                        draftViewModel.uiDraft = item
                        draftViewModel.filesArray.add(File(item.filePath))
                        //draftViewModel.durationOfLastAudio = item.length!!

                        val bundle = bundleOf("recordingItem" to draftViewModel.uiDraft)
                        findNavController().navigate(R.id.action_record_drafts_to_record_edit, bundle)
                    }
                },
                object : DraftAdapter.OnResumeItemClickListener {
                    override fun onResumeItemClick(position: Int, item: UIDraft) {
                        pbDrafts?.visibility = View.VISIBLE

                        try {
                            if(adapter?.mediaPlayer!!.isPlaying){
                                adapter?.mediaPlayer!!.stop()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        //Go to record fragment to continue recording
                        draftViewModel.uiDraft = item
                        draftViewModel.filesArray.add(File(item.filePath))
                        //draftViewModel.durationOfLastAudio = item.length!!

                        val bundle = bundleOf("recordingItem" to draftViewModel.uiDraft)
                        findNavController().navigate(R.id.action_record_drafts_to_record_fragment, bundle)
                    }
                }
            )
        }
        rvDrafts?.adapter = adapter
        rvDrafts?.setHasFixedSize(false)
    }

    private fun getDuplicatedTitle(currentTitle: String?) : String {
        if (currentTitle?.isNotEmpty() == true) {
            val previousTitleClean = currentTitle.substringBeforeLast("-").trim()
            val lastWord = currentTitle.substringAfterLast("-").trim()

            // this means that there is no - in the string, so it's the first time this item is duplicated, so we add - 1 at the end
            if (lastWord == currentTitle) {
                return "$previousTitleClean - 1"

                // this means that there is one - at the end of the string, so probably this is a previously duplicated item
            } else {

                // we get the number of duplication
                var number = lastWord.toIntOrNull()
                // if there is actually a number, then let's increment it
                if(number != null) {
                    number++

                    // if the value after the last - is not a number, then we'll ad just a 1 because it's the first time this item is duplicated
                } else {
                    number = 1
                }
                return "$previousTitleClean - $number"
            }

        } else {
            return getString(R.string.duplicated_draft)
        }

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
                hideEmptyScenario()
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

