package io.square1.limor.scenes.main.fragments.record

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.xxjy.amrwbenc.AmrWbEncoder
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.scenes.utils.*
import io.square1.limor.scenes.utils.Commons.*
import io.square1.limor.scenes.utils.CommonsKt.Companion.copyTo
import io.square1.limor.scenes.utils.statemanager.Step
import io.square1.limor.scenes.utils.waveform.MarkerSet
import io.square1.limor.scenes.utils.waveform.WaveformFragment
import io.square1.limor.uimodels.UIDraft
import io.square1.limor.uimodels.UITimeStamp
import kotlinx.android.synthetic.main.fragment_waveform.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import java.io.*
import java.util.*
import javax.inject.Inject
import kotlin.experimental.and
import kotlin.experimental.or


class EditFragment : WaveformFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private var recordingItem: UIDraft? = null
    private var initialFilePath: String? = null
    private var initialLength: Long = 0
    private var initialTimeStamps: ArrayList<UITimeStamp>? = null
    private var progressDialogQueue: Queue<ProgressDialog> = LinkedList()
    private var receiver: BroadcastReceiver? = null
    private var MEDIA_KEY = "soun"
    private var BROADCAST_UPDATE_DRAFTS = "update_drafts"
    var app: App? = null
    var hasAnythingChanged = false
    private lateinit var encoder: AmrEncoder


    companion object {
        val TAG: String = EditFragment::class.java.simpleName
        fun newInstance() = EditFragment()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 40f);

        listeners()
        bindViewModel()
        registerReceivers()
    }

    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        recordingItem = UIDraft()
        recordingItem = arguments!!["recordingItem"] as UIDraft
    }

    override fun onDestroy() {
        super.onDestroy()
        isEditMode = false
        context!!.unregisterReceiver(receiver)
    }

    override fun getFileName(): String {
        return recordingItem?.filePath!!
    }

    override fun populateMarkers() {
        if (recordingItem != null && recordingItem!!.timeStamps!!.size > 0) {
            for ((_, startSample, endSample) in recordingItem!!.timeStamps!!) {
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
            saveInitialState()
        }
        isInitialised = true
        updateUndoRedoButtons()
    }

    private fun saveInitialState() {
        initialFilePath = fileName
        initialLength = recordingItem!!.length!!
        initialTimeStamps = ArrayList()
        for ((duration, startSample, endSample) in recordingItem!!.timeStamps!!) {
            initialTimeStamps!!.add(UITimeStamp(duration, endSample, startSample))
        }
    }

    private fun listeners() {

        tvToolbarTitle?.text = getString(R.string.edit)

        tvRedo?.onClick { redoClick() }
        tvUndo?.onClick{ undoClick() }
        tvPaste?.onClick { pasteClick() }
        tvCopy?.onClick { copyClick() }
        tvDelete?.onClick { deleteClick() }

        closeButton.onClick {
            restoreToInitialState()
        }

        infoButton.onClick {
            openHowToEdit()
        }

        nextButtonEdit.onClick {
            openPublishFragment()
        }

    }

    private fun redoClick() {
        if (stepManager.lastRedoStep != null && stepManager.stepsToRedo.size > 0) {
            stepManager.addNewUndoStep(
                Step(
                    System.currentTimeMillis(),
                    fileName,
                    recordingItem!!.timeStamps
                )
            )
            recordingItem!!.timeStamps = stepManager.lastRedoStep.timeStamps
            fileName = stepManager.lastRedoStep.filePath
            loadFromFile(fileName)
            stepManager.handleLastRedoStep()
        }
        updateUndoRedoButtons()
    }

    private fun undoClick() {
        if (stepManager.lastUndoStep != null && stepManager.stepsToUndo.size > 0) {
            stepManager.addNewRedoStep(
                Step(
                    System.currentTimeMillis(),
                    fileName,
                    recordingItem!!.timeStamps
                )
            )
            fileName = stepManager.lastUndoStep.filePath
            loadFromFile(fileName)
            stepManager.handleLastUndoStep()
        }
        updateUndoRedoButtons()
    }

    private fun pasteClick() {
        if (!isEditMode) {
            return
        }
        if (editMarker == null || editMarker.startPos >= selectedMarker.startPos && editMarker.startPos <= selectedMarker.endPos) {
            showAlertOK(
                activity,
                getString(R.string.alert_title_oops),
                getString(R.string.paste_overlap_alert),
                null
            )
            return
        }
        Commons.showAlertYesNo(
            activity,
            getString(R.string.paste),
            getString(R.string.paste_prompt)
        ) { dialog: DialogInterface?, which: Int -> pasteMarkedChunk() }
    }

    private fun copyClick() {
        if (selectedMarker == null) {
            showAlertOK(
                activity,
                getString(R.string.alert_title_oops),
                getString(R.string.select_marker_firs_prompt),
                null
            )
            return
        }
        for (markerSet in markerSets) {
            if (markerSet.isEditMarker) {
                showAlertOK(
                    activity,
                    getString(R.string.alert_title_oops),
                    getString(R.string.cant_create_more_than_one_marker_prompt),
                    null
                )
                return
            }
        }
        //AnalyticsManager.getInstance().recordEditEvent(AnalyticsManager.RecordingEditEventType.RECORDING_EDIT_COPY_PASTE);
        //addMarker(selectedMarker.getStartPos(), selectedMarker.getStartPos() + 2, true, null);    //TODO JJ esta es la original
        addMarker(selectedMarker.startPos, selectedMarker.startPos + 2, true, null) //TODO JJ

    }

    private fun deleteClick() {
        if (selectedMarker == null) {
            showAlertOK(
                activity,
                getString(R.string.alert_title_oops),
                getString(R.string.select_marker_firs_prompt),
                null
            )
            return
        }
        showAlertOkCancel(
            activity,
            getString(R.string.remove),
            getString(R.string.remove_piece_of_audio_prompt),
            DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int -> deleteMarkedChunk() }
        )
    }

    private fun pasteMarkedChunk() {
        if (markerSets == null || markerSets.size == 0 || isPlayingPreview || isPlaying || selectedMarker == null) {
            return
        }
        stepManager.addNewUndoStep(
            Step(
                System.currentTimeMillis(),
                fileName,
                recordingItem!!.timeStamps
            )
        )
        showProgress(getString(R.string.progress_please_wait))
        Thread(Runnable {
            audioFilePaths = ArrayList()
            var copiedChunkPath: String? = null
            val outPathCopied =
                Objects.requireNonNull(Objects.requireNonNull(activity)!!.externalCacheDir).absolutePath + "/limor_record_chunk_copied.m4a"
            val startFrameCopied = waveformView.secondsToFrames(
                waveformView.pixelsToSeconds(
                    selectedMarker.startPos / NEW_WIDTH
                )
            ) //TODO JJ NEW 131020
            val endFrameCopied = waveformView.secondsToFrames(
                waveformView.pixelsToSeconds(
                    selectedMarker.endPos / NEW_WIDTH
                )
            ) //TODO JJ NEW 131020
            val outFileCopied = File(outPathCopied)
            try {
                soundFile.WriteFile(
                    outFileCopied,
                    startFrameCopied,
                    endFrameCopied - startFrameCopied
                )
                copiedChunkPath = outFileCopied.absolutePath
            } catch (e: Exception) {
                dismissProgress()
                e.printStackTrace()
            }
            //for (i = 0 i < 2; i++) {
            for (i in 0..1) {
                val outPath =
                    activity?.externalCacheDir!!.absolutePath + "/limor_record_chunk_" + i + ".m4a"
                val startTime =
                    waveformView.pixelsToSeconds(if (i == 0) 0 else (editMarker.startPos / NEW_WIDTH) - 5) //TODO JJ había un 10 e iba mal
                val endTime = waveformView.pixelsToSeconds(
                    if (i == 0) (editMarker.startPos / NEW_WIDTH) else waveformView.millisecsToPixels(
                        player.duration - 5
                    )
                ) //TODO JJ había un 10 e iba mal
                val startFrame = waveformView.secondsToFrames(startTime)
                val endFrame = waveformView.secondsToFrames(endTime)
                val outFile = File(outPath)
                try {
                    soundFile.WriteFile(outFile, startFrame, endFrame - startFrame)
                    if (i == 1) {
                        audioFilePaths.add(copiedChunkPath)
                    }
                    audioFilePaths.add(outFile.absolutePath)
                } catch (e: Exception) {
                    dismissProgress()
                    e.printStackTrace()
                }
            }
            fileName =
                activity?.externalCacheDir!!.absolutePath + "/limor_record_" + System.currentTimeMillis() + "_edited.m4a"
            try {
                val listMovies: MutableList<Movie> = ArrayList()
                for (filename in audioFilePaths) {
                    listMovies.add(MovieCreator.build(filename))
                }
                val listTracks: MutableList<Track> = LinkedList()
                for (movie in listMovies) {
                    for (track in movie.tracks) {
                        if (track.handler == MEDIA_KEY) {
                            listTracks.add(track)
                        }
                    }
                }
                val outputMovie = Movie()
                if (listTracks.isNotEmpty()) {
                    outputMovie.addTrack(AppendTrack(*listTracks.toTypedArray()))
                    //outputMovie.addTrack(AppendTrack(listTracks.toTypedArray()[listTracks.size])) //TODO JJ
                }

                val container = DefaultMp4Builder().build(outputMovie)
                val fileChannel = RandomAccessFile(String.format(fileName), "rws").channel
                container.writeContainer(fileChannel)
                fileChannel.close()

                Commons.deleteFiles(audioFilePaths); //TODO JJ Check this

                audioFilePaths = ArrayList()
                var copiedLength: Int = 0
                val startPosMilliseconds =
                    waveformView.pixelsToMillisecs(selectedMarker.startPos / NEW_WIDTH)
                val endPosMilliseconds =
                    waveformView.pixelsToMillisecs(selectedMarker.endPos / NEW_WIDTH)
                copiedLength = endPosMilliseconds - startPosMilliseconds
                activity?.runOnUiThread {
                    val timeStamps = ArrayList<UITimeStamp>()
                    if (markerSets != null && markerSets.size > 0) {
                        val iterator = markerSets.iterator()
                        while (iterator.hasNext()) {
                            val markerSet = iterator.next()
                            if (!markerSet.isEditMarker) {
                                var startPosMillisecondsAdjusted: Int
                                var endPosMillisecondsAdjusted: Int
                                if (waveformView.pixelsToMillisecs(markerSet.startPos / NEW_WIDTH) < waveformView.pixelsToMillisecs(
                                        editMarker.startPos / NEW_WIDTH
                                    )
                                ) {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.startPos / NEW_WIDTH
                                    )
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.endPos / NEW_WIDTH
                                    )
                                } else {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.startPos / NEW_WIDTH
                                    ) + copiedLength
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.endPos / NEW_WIDTH
                                    ) + copiedLength
                                }
                                handleTimeStamps(
                                    markerSet,
                                    timeStamps,
                                    startPosMillisecondsAdjusted,
                                    endPosMillisecondsAdjusted
                                )
                                iterator.remove()
                            } else if (markerSet.isEditMarker) {
                                //Middle marker
                                markerSet.middleMarker.visibility = View.GONE
                                markerSet.middleMarker = null
                                //Start marker
                                markerSet.startMarker.visibility = View.GONE
                                markerSet.startMarker = null
                                iterator.remove()
                            }
                        }

                    }

                    //addMarker(firstMarkerSet.getStartPos(), secondMarkerSet.getEndPos(), false, null);
                    //removeMarker(firstMarkerSet);

                    shouldReloadPreview = true
                    recordingItem?.timeStamps = timeStamps
                    recordingItem?.length = recordingItem?.length?.plus(copiedLength)

                    //Set the new file path to the object
                    val newFile = File(fileName)
                    recordingItem?.filePath = newFile.absolutePath

                    updateRecordingItem()
                    dismissProgress()
                    loadFromFile(fileName)
                    activity!!.sendBroadcast(Intent(BROADCAST_UPDATE_DRAFTS))
                    stepManager.resetRedoSteps()
                    isEditMode = false
                    editMarker = null

                }
                hasAnythingChanged = true

            } catch (ex: IOException) {
                dismissProgress()
                ex.printStackTrace()
            }

            //This line will remain the original marker when it is copied and pasted
            activity!!.runOnUiThread {
                addMarker(selectedMarker.startPos, selectedMarker.endPos, false, null); //TODO JJ
            }
        }).start()

    }

    protected fun deleteMarkedChunk() {
        if (markerSets == null || markerSets.size == 0 || isPlayingPreview || isPlaying || selectedMarker == null) {
            return
        }
        stepManager.addNewUndoStep(
            Step(System.currentTimeMillis(), fileName, recordingItem!!.timeStamps)
        )
        showProgress(getString(R.string.progress_please_wait))
        Thread(Runnable {
            audioFilePaths = ArrayList()
            for (i in 0..1) {
                val outPath =
                    activity!!.externalCacheDir.absolutePath + "/limor_record_chunk_" + i + ".m4a"
                val startTime =
                    waveformView.pixelsToSeconds(if (i == 0) 0 else (selectedMarker.endPos / NEW_WIDTH))
                val endTime = waveformView.pixelsToSeconds(
                    if (i == 0) (selectedMarker.startPos / NEW_WIDTH) else waveformView.millisecsToPixels(
                        player.duration - 10
                    )
                )
                val startFrame = waveformView.secondsToFrames(startTime)
                val endFrame = waveformView.secondsToFrames(endTime)
                val outFile = File(outPath)
                try {
                    soundFile.WriteFile(outFile, startFrame, endFrame - startFrame)
                    audioFilePaths.add(outFile.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            fileName =
                activity!!.externalCacheDir.absolutePath + "/limor_record_" + System.currentTimeMillis() + "_edited.m4a"
            try {
                val listMovies: MutableList<Movie> = ArrayList()
                for (filename in audioFilePaths) {
                    listMovies.add(MovieCreator.build(filename))
                }
                val listTracks: MutableList<Track> = LinkedList()
                for (movie in listMovies) {
                    for (track in movie.tracks) {
                        if (track.handler == MEDIA_KEY) {
                            listTracks.add(track)
                        }
                    }
                }
                val outputMovie = Movie()
                if (!listTracks.isEmpty()) {
                    outputMovie.addTrack(AppendTrack(*listTracks.toTypedArray()))
                }
                val container = DefaultMp4Builder().build(outputMovie)
                val fileChannel = RandomAccessFile(String.format(fileName), "rws").channel
                container.writeContainer(fileChannel)
                fileChannel.close()

                //SHOUtils.deleteFiles(audioFilePaths);
                audioFilePaths = ArrayList()
                val deletedLength: Int
                val startPosMilliseconds =
                    waveformView.pixelsToMillisecs(selectedMarker.startPos / NEW_WIDTH)
                val endPosMilliseconds =
                    waveformView.pixelsToMillisecs(selectedMarker.endPos / NEW_WIDTH)
                deletedLength = endPosMilliseconds - startPosMilliseconds
                activity!!.runOnUiThread {
                    removeMarker(selectedMarker)
                    val timeStamps = ArrayList<UITimeStamp>()
                    if (markerSets != null && markerSets.size > 0) {
                        val iterator =
                            markerSets.iterator()
                        while (iterator.hasNext()) {
                            val markerSet = iterator.next()
                            if (!markerSet.isEditMarker) {
                                var startPosMillisecondsAdjusted: Int
                                var endPosMillisecondsAdjusted: Int
                                if (waveformView.pixelsToMillisecs(markerSet.startPos / NEW_WIDTH) < startPosMilliseconds) {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.startPos / NEW_WIDTH
                                    )
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.endPos / NEW_WIDTH
                                    )
                                } else {
                                    startPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.startPos / NEW_WIDTH
                                    ) - deletedLength
                                    endPosMillisecondsAdjusted = waveformView.pixelsToMillisecs(
                                        markerSet.endPos / NEW_WIDTH
                                    ) - deletedLength
                                }
                                handleTimeStamps(
                                    markerSet,
                                    timeStamps,
                                    startPosMillisecondsAdjusted,
                                    endPosMillisecondsAdjusted
                                )
                                iterator.remove()
                                shouldReloadPreview = true
                            }
                        }
                    }
                    recordingItem!!.timeStamps = timeStamps
                    recordingItem!!.length = recordingItem!!.length!! - deletedLength
                    recordingItem!!.filePath = fileName
                    updateRecordingItem()
                    dismissProgress()
                    loadFromFile(fileName)
                    activity!!.sendBroadcast(Intent(BROADCAST_UPDATE_DRAFTS))
                    stepManager.resetRedoSteps()
                    isEditMode = false
                    selectedMarker = null
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
                dismissProgress()
            }
            hasAnythingChanged = true
        }).start()
    }

    private fun handleTimeStamps(
        markerSet: MarkerSet,
        timeStamps: ArrayList<UITimeStamp>,
        startPos: Int,
        endPos: Int
    ) {
        val timeStamp = UITimeStamp()
        timeStamp.duration = startPos
        timeStamp.endSample = endPos
        timeStamp.duration = endPos - startPos
        timeStamps.add(timeStamp)



        markerSet.startMarker.visibility = View.GONE
        markerSet.startMarker = null
        markerSet.middleMarker.visibility = View.GONE
        markerSet.middleMarker = null
        markerSet.endMarker.visibility = View.GONE
        markerSet.endMarker = null

        //addMarker(selectedMarker.startPos, selectedMarker.startPos + 2, true, null) //TODO JJ
        //addMarker(startPos, endPos, false, null);//TODO JJ

        //rlPreviewSection.setAlpha(1.0f);
        //rlPreviewSection.alpha = 0.4f //Set alpha to 60% of visibility //TODO JJ


        //updateDisplay(); //TODO JJ New
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
                    intent.action.contains("BROADCAST_OPEN_HOW_TO_EDIT") -> {
                        openHowToEdit()
                    }
                    intent.action.contains("BROADCAST_OPEN_PUBLISH_SCREEN") -> {
                        openPublishFragment()
                    }
                    intent.action.contains("BROADCAST_RESTORE_INITIAL_RECORDING") -> {
                        restoreToInitialState()
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
            recordingItem!!.editedFilePath = editedWithMarkersFileName
        } else {
            recordingItem!!.editedFilePath = ""
        }

        recordingItem!!.timeStamps = timeStamps
        updateRecordingItem()

        val bundle = bundleOf("recordingItem" to recordingItem)
        findNavController().navigate(R.id.action_record_edit_to_record_publish, bundle)

    }

    private fun rename(from: File, to: File): Boolean {
        return from.parentFile.exists() && from.exists() && from.renameTo(to)
    }

    private fun restoreToInitialState() {
        //TODO JJ Esto había antes
//        recordingItem!!.timeStamps = initialTimeStamps
//        recordingItem!!.length = initialLength
//        recordingItem!!.filePath = initialFilePath
//        updateRecordingItem()
//        activity!!.sendBroadcast(Intent(BROADCAST_UPDATE_DRAFTS))
//        hasAnythingChanged = false
//
//        findNavController().popBackStack()

        //TODO JJ Guardar los cambios realizados
        handlePause()
        handlePausePreview()
        //DataManager.getInstance().setSkipRecordScreen(true);
        val timeStamps = ArrayList<UITimeStamp>()
        if (markerSets != null && markerSets.size > 0) {
            for (markerSet in markerSets) {
                if (!markerSet.isEditMarker) {
                    val startPosMilliseconds = waveformView.pixelsToMillisecs(markerSet.startPos)
                    val endPosMilliseconds = waveformView.pixelsToMillisecs(markerSet.endPos)
                    val timeStamp = UITimeStamp()
                    timeStamp.startSample = startPosMilliseconds
                    timeStamp.endSample = endPosMilliseconds
                    timeStamp.duration = endPosMilliseconds - startPosMilliseconds
                    timeStamps.add(timeStamp)
                }
            }
            saveNewFileFromMarkers(false)


            recordingItem!!.filePath = editedWithMarkersFileName
            recordingItem!!.editedFilePath = editedWithMarkersFileName  //TODO JJ este formato de fichero es m4a y está en otro directory
        } else {
            //TODO JJ This should be commented
            //recordingItem!!.filePath = ""
            //recordingItem!!.editedFilePath = ""
        }

        if(recordingItem!!.filePath!!.endsWith("m4a")){

            val flacFile = File(recordingItem!!.filePath)
            val callback: IConvertCallback = object : IConvertCallback {
                override fun onSuccess(convertedFile: File?) {

                    val wavFileInCustomFolder = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/" + System.currentTimeMillis() + ".wav")
                    convertedFile?.copyTo(wavFileInCustomFolder)

                    try {
                        deleteFile(recordingItem!!.filePath)
                        deleteFile(convertedFile?.absolutePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    recordingItem!!.editedFilePath = ""
                    recordingItem!!.filePath = wavFileInCustomFolder.absolutePath
                    recordingItem!!.timeStamps = timeStamps
                    recordingItem!!.length = player.duration.toLong() //si modifico el audio y le doy atrás, el record sigue mostrando la duración del audio original, no el editado

                    draftViewModel.uiDraft = recordingItem!!

                    draftViewModel.filesArray.clear()
                    draftViewModel.filesArray.add(File(recordingItem?.filePath))
                    draftViewModel.durationOfLastAudio = player.duration.toLong()
                    draftViewModel.continueRecording = true

                    findNavController().popBackStack()
                }

                override fun onFailure(error: java.lang.Exception?) {
                    // Oops! Something went wrong
                    toast(error?.printStackTrace().toString())
                }
            }
            AndroidAudioConverter.with(context!!) // Your current audio file
                .setFile(flacFile) // Your desired audio format
                .setFormat(AudioFormat.WAV) // An callback to know when conversion is finished
                .setCallback(callback) // Start conversion
                .convert()

        }else{
            // So fast? Love it!
            draftViewModel.continueRecording = true
            recordingItem!!.timeStamps = timeStamps
            recordingItem!!.length = player.duration.toLong() //si modifico el audio y le doy atrás, el record sigue mostrando la duración del audio original, no el editado
            draftViewModel.durationOfLastAudio = player.duration.toLong()
            updateRecordingItem()

            findNavController().popBackStack()
        }




    }

    private fun showProgress(message: String?) {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setCancelable(true)
        if (message != null) progressDialog.setMessage(message)
        progressDialog.show()
        progressDialogQueue.add(progressDialog)
    }

    private fun dismissProgress() {
        val progressDialog = progressDialogQueue.poll()
        progressDialog?.dismiss()
    }

    private fun updateRecordingItem() { //Update recording item in database  //TODO JJ
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
            draftViewModel.uiDraft = recordingItem!!

            draftViewModel.filesArray.clear()
            if(!draftViewModel.filesArray.contains(File(recordingItem?.filePath))){
                draftViewModel.filesArray.add(File(recordingItem?.filePath))
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

    private fun showAlertOkCancel(
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
            context.getString(
                R.string.cancel
            )
        )
    }


}
