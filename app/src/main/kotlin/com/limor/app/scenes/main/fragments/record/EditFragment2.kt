package com.limor.app.scenes.main.fragments.record

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.limor.app.App
import com.limor.app.R
import com.limor.app.scenes.main.viewmodels.DraftViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.waveform.WaveformFragment
import com.limor.app.uimodels.UIDraft
import com.limor.app.uimodels.UITimeStamp
import kotlinx.android.synthetic.main.fragment_waveform.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.sdk23.listeners.onClick
import java.io.File
import java.util.*
import javax.inject.Inject


class EditFragment2 : WaveformFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private var uiDraft: UIDraft? = null
    private var initialFilePath: String? = null
    private var initialLength: Long = 0
    private var initialTimeStamps: ArrayList<UITimeStamp>? = null
    private var progressDialogQueue: Queue<ProgressDialog> = LinkedList()
    private var receiver: BroadcastReceiver? = null
    var app: App? = null
    private var hasAnythingChanged = false
//    private lateinit var encoder: AmrEncoder


    companion object {
        val TAG: String = EditFragment::class.java.simpleName
        fun newInstance() = EditFragment()
        private const val BROADCAST_UPDATE_DRAFTS = "update_drafts"
        private const val MEDIA_KEY = "soun"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 40f)

        listeners()
        bindViewModel()
        registerReceivers()
    }

    override fun populateMarkers() {
        if (uiDraft != null && uiDraft!!.timeStamps!!.size > 0) {
            for ((_, startSample, endSample) in uiDraft!!.timeStamps!!) {
                if (startSample!! > 0 && endSample!! < player.duration) {
                    addMarker(
                        waveformView.millisecsToPixels(startSample), waveformView.millisecsToPixels(
                            endSample
                        ), false, R.color.white
                    )
                }
            }
        }
        if (!isInitialised) {
//            saveInitialState()
        }
        isInitialised = true
    }

    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)
        }
    }

    override fun getFileName(): String {
        return uiDraft?.filePath!!
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        uiDraft = UIDraft()
        uiDraft = arguments!!["recordingItem"] as UIDraft
    }

    override fun onDestroy() {
        super.onDestroy()
        isEditMode = false
        context!!.unregisterReceiver(receiver)
    }


    private fun listeners() {

        tvToolbarTitle?.text = getString(R.string.edit)

        tvRedo?.onClick {  }
        tvUndo?.onClick{  }
        tvPaste?.onClick {  }
        tvCopy?.onClick {  }
        tvDelete?.onClick {  }

        closeButton.onClick {

        }

        infoButton.onClick {
            openHowToEdit()
        }

        nextButtonEdit.onClick {
            openPublishFragment()
        }

    }

    private fun registerReceivers() {

        // your oncreate code should be
        val filter = IntentFilter()
        filter.addAction("BROADCAST_OPEN_HOW_TO_EDIT")
        filter.addAction("BROADCAST_OPEN_PUBLISH_SCREEN")
        filter.addAction("BROADCAST_RESTORE_INITIAL_RECORDING")
        receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                //do something based on the intent's action
                when {
                    intent.action!!.contains("BROADCAST_OPEN_HOW_TO_EDIT") -> {
                        openHowToEdit()
                    }
                    intent.action!!.contains("BROADCAST_OPEN_PUBLISH_SCREEN") -> {
                        openPublishFragment()
                    }
                    intent.action!!.contains("BROADCAST_RESTORE_INITIAL_RECORDING") -> {

                    }
                }
            }
        }
        context!!.registerReceiver(receiver, filter)
    }

    private fun openHowToEdit() {
        showAlertOK(
            activity,
            getString(R.string.how_to_edit_title),
            getString(R.string.how_to_edit_description),
            null
        )
    }

    private fun openPublishFragment() {
        handlePause()
        handlePausePreview()
        //DataManager.getInstance().setSkipRecordScreen(true);
        val timeStamps = ArrayList<UITimeStamp>()
        if (markerSets != null && markerSets.size > 0) {
            for (markerSet in markerSets) {
                if (!markerSet.isEditMarker) {
                    val startPosMilliseconds =
                        waveformView.pixelsToMillisecs(markerSet.startPos)
                    val endPosMilliseconds =
                        waveformView.pixelsToMillisecs(markerSet.endPos)
                    val timeStamp = UITimeStamp()
                    timeStamp.startSample = startPosMilliseconds
                    timeStamp.endSample = endPosMilliseconds
                    timeStamp.duration = endPosMilliseconds - startPosMilliseconds
                    timeStamps.add(timeStamp)
                }
            }
            saveNewFileFromMarkers(false)
            uiDraft!!.editedFilePath = editedWithMarkersFileName
        } else {
            uiDraft!!.editedFilePath = ""
        }

        uiDraft!!.timeStamps = timeStamps
        updateDraftToDB()

        val bundle = bundleOf("recordingItem" to uiDraft)
        findNavController().navigate(R.id.action_record_edit_to_record_publish, bundle)

    }

    private fun updateDraftToDB() { //Update recording item in database  //TODO JJ
//        try {
//            draftViewModel.uiDraft = recordingItem!!
//            if (!draftViewModel.filesArray.contains(File(recordingItem?.filePath))){
//                draftViewModel.filesArray.add(File(recordingItem?.filePath))
//            }
//
//            draftViewModel.continueRecording = true
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        try {
            draftViewModel.uiDraft = uiDraft!!

            draftViewModel.filesArray.clear()
            if(!draftViewModel.filesArray.contains(File(uiDraft?.filePath))){
                draftViewModel.filesArray.add(File(uiDraft?.filePath))
            }

            draftViewModel.continueRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showAlertOK(
        context: Context?,
        title: String?,
        message: String?,
        listener: DialogInterface.OnClickListener?
    ) {
        Commons.showAlertCustomButtons(
            context,
            title,
            message,
            listener,
            context!!.getString(R.string.ok),
            null,
            null
        )
    }

}
