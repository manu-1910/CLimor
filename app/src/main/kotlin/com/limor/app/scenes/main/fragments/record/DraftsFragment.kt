package com.limor.app.scenes.main.fragments.record


import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
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

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import kotlinx.android.synthetic.main.sheet_more_draft.view.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


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
        btnRecordCast?.onClick {
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
            if (adapter?.mediaPlayer!!.isPlaying) {
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
            draftViewModel = ViewModelProvider(it, viewModelFactory)
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
        btnEditToolbarUpdate?.visibility = View.GONE
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
                        if (!comesFromEditMode) {
                        }
                    }
                },
                object : DraftAdapter.OnDeleteItemClickListener {
                    override fun onDeleteItemClick(position: Int) {
                        showDeleteDialog(position)
                    }
                },
                object : DraftAdapter.OnDuplicateItemClickListener {
                    override fun onDuplicateItemClick(position: Int) {
                        val newItem = draftsLocalList[position]

                        var shouldContinue = false
                        newItem.filePath?.let { path ->
                            val f = File(path)
                            shouldContinue = f.exists()
                        }

                        if (!shouldContinue) {
                            alert(getString(R.string.error_accessing_draft_file)) {
                                okButton { }
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
                object : DraftAdapter.OnChangeNameClickListener {
                    override fun onNameChangedClick(item: UIDraft, position: Int, newName: String) {
                        item.title = newName
                        adapter?.notifyItemChanged(position)
                        draftViewModel.uiDraft = item
                        insertDraftsTrigger.onNext(Unit)
                    }
                },
                object : DraftAdapter.OnResumeItemClickListener {
                    override fun onResumeItemClick(position: Int, item: UIDraft) {
                        pbDrafts?.visibility = View.VISIBLE

                        try {
                            if (adapter?.mediaPlayer!!.isPlaying) {
                                adapter?.mediaPlayer!!.stop()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        //Go to record fragment to continue recording
                        Timber.d("Draft ${item.isNewRecording}")
                        draftViewModel.uiDraft = item.apply {
                            isNewRecording = false
                        }
                        draftViewModel.filesArray.add(File(item.filePath))
                        //draftViewModel.durationOfLastAudio = item.length!!

                        val bundle = bundleOf("recordingItem" to draftViewModel.uiDraft)
                        findNavController().navigate(
                            R.id.action_record_drafts_to_record_fragment,
                            bundle
                        )
                    }
                },
                object : DraftAdapter.OnMoreItemClickListener {
                    override fun onMoreItemClick(position: Int, item: UIDraft) {
                        showMoreDialog(position, item)
                    }
                }
            )
        }
        rvDrafts?.adapter = adapter
        rvDrafts?.setHasFixedSize(false)
    }

    private fun showDeleteDialog(position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_delete_cast, null)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.yesButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelButton)

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)
        val dialog: AlertDialog = dialogBuilder.create()

        positiveButton.onClick {
            pbDrafts?.visibility = View.VISIBLE

            try {
                if (adapter?.mediaPlayer!!.isPlaying) {
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
            if(draftsLocalList.size == 0){
                draftViewModel.uiDraft = null
            }
            //rvDrafts?.adapter?.notifyItemRemoved(position)
            rvDrafts?.adapter?.notifyDataSetChanged()
            //rvDrafts?.adapter?.notifyItemRangeChanged(0, draftsLocalList.size)
            dialog.dismiss()
        }

        cancelButton.onClick {
            dialog.dismiss()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset);
            show()
        }
    }


    private fun showMoreDialog(position: Int, draft: UIDraft) {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val dialogView = layoutInflater.inflate(R.layout.sheet_more_draft, null)

        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setCancelable(true)

        dialogView.layoutChangeName.setOnClickListener {
            bottomSheetDialog.dismiss()
            adapter?.changeNameClicked(draft, position)
        }

        dialogView.layoutDuplicateCast.setOnClickListener {
            bottomSheetDialog.dismiss()
            adapter?.stopMediaPlayer()
            adapter?.duplicateListener?.onDuplicateItemClick(position)
        }

        dialogView.layoutDeleteCast.setOnClickListener {
            bottomSheetDialog.dismiss()
            adapter?.deleteClicked(position)
        }

        bottomSheetDialog.apply {
            show()
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun getDuplicatedTitle(currentTitle: String?): String {
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
                if (number != null) {
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
        btnEditToolbarUpdate?.onClick { null }
    }


    private fun hideEmptyScenario() {
        emptyScenarioDraftsLayout?.visibility = View.GONE
        rvDrafts?.visibility = View.VISIBLE
    }


    private fun loadDrafts() {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        //Observer of LiveData ViewModel
        draftViewModel.loadDraftRealm()?.observe(viewLifecycleOwner, Observer<List<UIDraft>> {
            draftsLocalList.clear()
            if (it.isNotEmpty()) {
                it.sortedByDescending { draft ->
                    CommonsKt.getDateFromString(draft.date!!)
                }
                    .map { uiRealmDraft ->
                        pbDrafts?.visibility = View.GONE

                        draftsLocalList.add(uiRealmDraft)
                    }

                //Continue editing
                if (btnEditToolbarUpdate?.text!!.trim() == getString(R.string.btnDone)) {
                    comesFromEditMode = true
                    btnEditToolbarUpdate?.text = getString(R.string.btnDone)
                    //Edit pressed, show mask
                    for (draft in draftsLocalList) {
                        draft.isEditMode = true
                    }
                }

                rvDrafts?.adapter?.notifyDataSetChanged()
                hideEmptyScenario()
            } else {
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

        output.response.observe(viewLifecycleOwner, Observer {
            if (it) {
                toast(getString(R.string.draft_deleted))
            } else {
                toast(getString(R.string.draft_not_deleted))
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
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

        output.response.observe(viewLifecycleOwner, Observer {
            if (it) {
                toast(getString(R.string.draft_inserted))
            } else {
                toast(getString(R.string.draft_not_inserted))
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.draft_not_inserted_error))
        })
    }


    override fun onResume() {
        super.onResume()
        val act = requireActivity() as RecordActivity
        act.initScreenBehaviour()
        pbDrafts?.visibility = View.VISIBLE
        getDraftsTrigger.onNext(Unit)
    }


}

