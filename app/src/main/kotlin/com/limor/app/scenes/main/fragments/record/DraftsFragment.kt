package com.limor.app.scenes.main.fragments.record


import android.app.AlertDialog
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.fragment_drafts.*
import kotlinx.android.synthetic.main.fragment_drafts_empty_scenario.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
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
    private lateinit var adapter: DraftAdapter
    private var comesFromEditMode = false
    private var btnEditToolbarUpdate: Button? = null
    private var btnCloseToolbar: ImageButton? = null
    private var tvTitleToolbar: TextView? = null
    var app: App? = null

    private lateinit var seekUpdater: Runnable
    private val seekHandler: Handler = Handler()

    var mediaPlayer = MediaPlayer()

    var currentPlayingDraftId = 0L


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
        seekUpdater = object : Runnable {
            override fun run() {
                seekHandler.postDelayed(this, 150)
                mediaPlayer.let {
                    if (it.isPlaying) {
                        val currentPosition = it.currentPosition
//                        Timber.tag(TAG).d("We are updating the playing state. The current status is pos[$currentPosition]")
                        sbAudioProgress?.let { seekBar ->
//                            Timber.tag(TAG).d("We are updating the seekbar $seekBar")
                            seekBar.progress = currentPosition
                        }
                        tvAudioTimePass?.let { tvPass ->
//                            Timber.tag(TAG).d("We are updating the tv $tvPass")
                            tvPass.text = CommonsKt.calculateDurationMediaPlayer(
                                    currentPosition
                            )
                        }
                    }
                }
            }
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
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
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
        adapter = DraftAdapter(
                requireContext(),
                draftsLocalList,
                object : DraftAdapter.OnItemClickListener {
                    override fun onItemClick(item: UIDraft, position: Int) {
                        initPlayerView(item, position)
                    }
                },
                object : DraftAdapter.OnDeleteItemClickListener {
                    override fun onDeleteItemClick(position: Int) {
                        alert(getString(R.string.confirmation_delete_draft)) {
                            okButton {
                                pbDrafts?.visibility = View.VISIBLE

                                try {
                                    if (adapter.mediaPlayer.isPlaying) {
                                        adapter.mediaPlayer.stop()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                draftViewModel.uiDraft = draftsLocalList[position]
                                deleteDraftsTrigger.onNext(Unit)

                                //Remove the audio file from the folder
                                try {
                                    val file = File(draftsLocalList[position].filePath!!)
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
                            cancelButton { }
                        }.show()
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
                object : DraftAdapter.OnEditItemClickListener {
                    override fun onEditItemClick(item: UIDraft) {
                        pbDrafts?.visibility = View.VISIBLE

                        try {
                            if (adapter?.mediaPlayer!!.isPlaying) {
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

                // This is deprecated, it's old code. This will never be called.
                object : DraftAdapter.OnChangeNameClickListener {
                    override fun onNameChangedClick(item: UIDraft, position: Int, newName: String) {
                        item.title = newName
                        adapter.notifyItemChanged(position)
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
                        draftViewModel.uiDraft = item
                        draftViewModel.filesArray.add(File(item.filePath))
                        //draftViewModel.durationOfLastAudio = item.length!!

                        val bundle = bundleOf("recordingItem" to draftViewModel.uiDraft)
                        findNavController().navigate(R.id.action_record_drafts_to_record_fragment, bundle)
                    }
                }
        )
        rvDrafts?.adapter = adapter
        rvDrafts?.setHasFixedSize(false)
    }

    private fun initPlayerView(draft: UIDraft, position: Int) {
        if (!comesFromEditMode) {
            var currentDurationInMillis = 0
            val uri: Uri = Uri.parse(draft.filePath)
            uri.path?.let {
                val f = File(it)
                if (f.exists()) {
                    val mmr = MediaMetadataRetriever()
                    try {
                        mmr.setDataSource(context, uri)
                        val durationStr =
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        currentDurationInMillis = durationStr.toInt()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Timber.d("Couldn't read metadata and duration. This could be because the file you're trying to access is corrupted")
                    }
                }
            }


            // seekBar
            sbAudioProgress.max = currentDurationInMillis
            if (draft.id == currentPlayingDraftId) {
                sbAudioProgress.progress = mediaPlayer.currentPosition
            } else {
                sbAudioProgress.progress = 0
            }
            sbAudioProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // if we click on the current playing item seekbar, then we'll seek the audio position
                    if (fromUser && draft.id == currentPlayingDraftId) {
                        mediaPlayer.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            // textViews
            if (draft.id == currentPlayingDraftId) {
                tvAudioTimePass.text = CommonsKt.calculateDurationMediaPlayer(mediaPlayer.currentPosition)
            } else {
                tvAudioTimePass.text = "00:00"
            }
            tvAudioDuration.text = CommonsKt.calculateDurationMediaPlayer(currentDurationInMillis)

            itemAudioPlayer.visibility = View.VISIBLE

            imageButtonPlayPause.setOnClickListener {
                if (draft.id == currentPlayingDraftId) {
                    onCurrentPlayingDraftPlayClicked()
                } else {
                    onOtherDraftPlayClicked(draft)
                }
            }

            // Forward button
            ibAudioFwd.onClick {
                onForwardClicked(draft)
            }

            // Rewind button
            ibAudioRew.onClick {
                onRewindClicked(draft)
            }


            // More button -> show options menu
            btnAudioMore.onClick {
                showMorePopupMenu(draft, position)
            }


            tvResumeRecord.onClick {
                stopMediaPlayer()
                draft.length = currentDurationInMillis.toLong()
                adapter?.resumeListener?.onResumeItemClick(position, draft)
            }

        }
    }

    private fun showMorePopupMenu(currentDraft: UIDraft, position: Int) {
        //creating a popup menu
        val popup = PopupMenu(context, btnAudioMore)
        //inflating menu from xml resource
        popup.inflate(R.menu.menu_drafts_iems_adapter)
        //adding click listener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_duplicate_cast -> {
                    stopMediaPlayer()
                    adapter?.duplicateListener?.onDuplicateItemClick(position)
                    true
                }
                R.id.menu_delete_cast -> {
                    deleteClicked(position)
                    true
                }
                R.id.menu_change_name_cast -> {
                    changeNameClicked(currentDraft, position)
                    true
                }
                else -> false
            }
        }
        //displaying the popup
        popup.show()
    }

    private fun changeNameClicked(currentDraft: UIDraft, position: Int) {
        showSaveDraftAlert(currentDraft.title) { newName ->
            adapter.changeNameListener.onNameChangedClick(currentDraft, position, newName)
        }
    }

    private fun showSaveDraftAlert(currentName: String?, onPositiveClicked: (title: String) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = requireContext().layoutInflater
        dialogBuilder.setTitle(requireContext().getString(R.string.edit_draft_title_dialog_title))
        val dialogLayout = inflater.inflate(R.layout.dialog_with_edittext, null)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelButton)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)
        editText.setText(currentName)
        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)
        val dialog: AlertDialog = dialogBuilder.show()

        positiveButton.onClick {
            onPositiveClicked(editText.text.toString())
            dialog.dismiss()
        }

        cancelButton.onClick {
            dialog.dismiss()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                positiveButton.isEnabled = !p0.isNullOrEmpty()
            }
        })
    }

    private fun deleteClicked(position: Int) {
        stopMediaPlayer()
        itemAudioPlayer.visibility = View.GONE
        adapter?.deleteListener?.onDeleteItemClick(position)
    }

    private fun onOtherDraftPlayClicked(draft: UIDraft) {
        stopMediaPlayer()
        val currentDraft = draft
        var shouldContinue = false
        currentDraft.filePath?.let { path ->
            val f = File(path)
            if (f.exists())
                shouldContinue = true
        }

        // if the file doesn't exist, we shouldn't continue because we won't be able to play it
        if (!shouldContinue) {
            requireContext().alert(requireContext().getString(R.string.error_accessing_draft_file)) {
                okButton { }
            }.show()
        } else {
            // if it's playing or not, then we have to stop the current player draft playing and setup this new draft player
            currentPlayingDraftId = draft.id ?: 0L


            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                mediaPlayer.stop()
                mediaPlayer.release()
            }

            imageButtonPlayPause?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.pause))
//            enableRewAndFwdButtons(true)

            mediaPlayer = MediaPlayer()
            mediaPlayer.setOnCompletionListener {
                onCompletionListener()
            }
            mediaPlayer.setDataSource(currentDraft.filePath)
            try {
                mediaPlayer.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d("The mediaplayer could not be prepared, probably because the file is corrupted")
                requireContext().alert(requireContext().getString(R.string.error_file_corrupted)) {
                    okButton { }
                }
                return
            }
            mediaPlayer.setOnPreparedListener {
                it.start()
                sbAudioProgress?.progress?.let { newProgress ->
                    mediaPlayer.seekTo(newProgress)

                    // this is to restart the audio after you have stopped it at the end of the track
                    if (mediaPlayer.currentPosition == mediaPlayer.duration)
                        mediaPlayer.seekTo(0)
                }

                seekHandler.post(seekUpdater)
            }
        }
    }

    private fun onCompletionListener() {
        imageButtonPlayPause?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.play))
        sbAudioProgress?.progress = sbAudioProgress?.max ?: mediaPlayer.duration
//        enableRewAndFwdButtons(false)
        mediaPlayer.pause()
    }

    private fun onCurrentPlayingDraftPlayClicked() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            imageButtonPlayPause?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.play))
//            enableRewAndFwdButtons(false)

            // if it's not playing, we just have to play it and change buttons images
        } else {
            // this is to restart the audio after you have stopped it at the end of the track
            if (mediaPlayer.currentPosition == mediaPlayer.duration) {
                mediaPlayer.seekTo(0)
            }
            mediaPlayer.start()
            imageButtonPlayPause?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.pause))
//            enableRewAndFwdButtons(true)
        }
    }

    private fun onRewindClicked(draft: UIDraft) {
        try {
            val nextPosition = sbAudioProgress.progress - 30000
            sbAudioProgress.progress = nextPosition
            if (currentPlayingDraftId == draft.id)
                mediaPlayer.seekTo(nextPosition)
        } catch (e: Exception) {
            Timber.d("mediaPlayer.seekTo rewind overflow")
        }
    }

    private fun onForwardClicked(draft: UIDraft) {
        try {
            var nextPosition = sbAudioProgress.progress + 30000
            if (nextPosition > sbAudioProgress.max)
                nextPosition = sbAudioProgress.max
            sbAudioProgress.progress = nextPosition
            if (currentPlayingDraftId == draft.id) {
                mediaPlayer.seekTo(nextPosition)
            }
        } catch (e: Exception) {
            Timber.d("mediaPlayer.seekTo forward overflow")
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
        //Observer of LiveData ViewModel
        draftViewModel.loadDraftRealm()?.observe(this, Observer<List<UIDraft>> {
            draftsLocalList.clear()
            if (it.isNotEmpty()) {
                it.map { uiRealmDraft ->
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
            } else {
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

    private fun stopMediaPlayer() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (e: Exception) {
            println("Exception stopping media player inside DraftAdapter")
        }
    }


}

