package com.limor.app.scenes.main.fragments.record

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.limor.app.App
import com.limor.app.R
import com.limor.app.scenes.main.viewmodels.DraftViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.Commons.deleteFile
import com.limor.app.scenes.utils.CommonsKt.Companion.copyTo
import com.limor.app.scenes.utils.statemanager.Step
import com.limor.app.scenes.utils.waveform.MarkerSet
import com.limor.app.scenes.utils.waveform.WaveformFragment
import com.limor.app.uimodels.UIDraft
import com.limor.app.uimodels.UITimeStamp
import kotlinx.android.synthetic.main.fragment_waveform.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*
import javax.inject.Inject


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
        ) { _: DialogInterface?, _: Int -> pasteMarkedChunk() }
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
        addMarker(selectedMarker.startPos, selectedMarker.startPos + 2, true, null)
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
            DialogInterface.OnClickListener { _: DialogInterface?, _: Int -> deleteMarkedChunk() }
        )
    }

    private fun getStartFrameCopied(): Int {
        val posMarker = selectedMarker.startPos
        return waveformView.secondsToFrames(waveformView.pixelsToSeconds(posMarker / NEW_WIDTH))
    }

    private fun getStartSecondCopied(): Double {
        val posMarker = selectedMarker.startPos
        return waveformView.pixelsToSeconds(posMarker / NEW_WIDTH)
    }

    private fun getEndFrameCopied() : Int {
        return waveformView.secondsToFrames(
            waveformView.pixelsToSeconds(
                selectedMarker.endPos / NEW_WIDTH
            )
        )
    }

    private fun getEndSecondCopied() : Double {
        return waveformView.pixelsToSeconds(
            selectedMarker.endPos / NEW_WIDTH
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
            // we prepare an output path to copy
            val copiedChunkPath = activity?.externalCacheDir?.absolutePath + "/limor_record_chunk_copied.m4a"

            // we get first frame where we have to copy
            val startSecondCopied = getStartSecondCopied().toFloat()

            // we get the last frame where we have to copy
            val endSecondCopied = getEndSecondCopied().toFloat()

            // and write that new temporary file with the just copied piece of audio
            try {
                writeSoundTemporaryFile(copiedChunkPath, startSecondCopied, endSecondCopied)
            } catch (e: Exception) {
                dismissProgress()
                Timber.d("Error writing copied chunk when pasting -> ${e.message}")
                runOnUiThread {
                    alert("There was an error copying your audio") {
                        okButton {  }
                    }.show()
                }

                // if this step fails, then we have to exit runnable
                return@Runnable
            }


            // now we have to split the audio in 2 files:
            // the left side of the paste position
            // the right side of the paste position
            val outPathLeft = activity?.externalCacheDir!!.absolutePath + "/limor_record_chunk_0.m4a"
            val outPathRight = activity?.externalCacheDir!!.absolutePath + "/limor_record_chunk_1.m4a"
            val startTimeLeft = 0f
            val endTimeLeft = waveformView.pixelsToSeconds(editMarker.startPos / NEW_WIDTH).toFloat()
            val startTimeRight = endTimeLeft + 0.1f
            val endTimeRight = player.duration / 1000.0f

            // this list will hold the filenames of the audio chunks that will be merged in a single file later
            val audioFilePaths = ArrayList<String>()
            try {
                // endTimeLeft will be 0 when the user wants to paste just at the beginning of the audio.
                // This check is because if the user wants to paste just in the beginning of the audio, there won't be a "left" side, because
                // the copied chunk will be at the beginning and the rest of the audio will be at the end
                if(endTimeLeft > 0) {
                    writeSoundTemporaryFile(outPathLeft, startTimeLeft, endTimeLeft)
                    audioFilePaths.add(outPathLeft)
                }

                audioFilePaths.add(copiedChunkPath)

                // startTimeRight will be greater or equal to endTimeRight when the user wants to paste just at the end of the audio.
                // This checks is because if the user wants to paste just at the end of the audio, there won't be a "right" side, because
                // the copied chunk will be just at the end and the rest will be at the beginning
                if(startTimeRight < endTimeRight) {
                    writeSoundTemporaryFile(outPathRight, startTimeRight, endTimeRight)
                    audioFilePaths.add(outPathRight)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dismissProgress()
                Timber.d("Error writing files in chunks when pasting -> ${e.message}")
                runOnUiThread {
                    alert("There was an error pasting your audio") {
                        okButton {  }
                    }.show()
                }

                // if this step fails, then we have to exit runnable
                return@Runnable
            }

            // and finally, we have to merge all those 3 chunks in one single file
            fileName = activity?.externalCacheDir!!.absolutePath + "/limor_record_" + System.currentTimeMillis() + "_edited.m4a"
            try {
                // in order to achieve that, we have to create a list of movies from the previous created files
                val listMovies = ArrayList<Movie>()
                for (filename in audioFilePaths) {
                    listMovies.add(MovieCreator.build(filename))
                }

                val listTracks = ArrayList<Track>()

                // and we have to concatenate a new moview from all the tracks of all the previous movies
                val outputMovie = Movie()
                for (movie in listMovies) {
                    for (track in movie.tracks) {
                        if (track.handler == MEDIA_KEY) {
                            listTracks.add(track)
                        }
                    }
                }

                if(listTracks.isEmpty()) {
                    dismissProgress()
                    Timber.d("The list of tracks is empty for some reason")
                    runOnUiThread {
                        alert("There was an error pasting your audio") {
                            okButton {  }
                        }.show()
                    }

                    // if this step fails, then we have to exit runnable
                    return@Runnable
                }

                // In this line what we are doing is to create a single track with all the tracks in the track list
                // you may think that you could just call addTrack for each track, but the result won't be the same
                outputMovie.addTrack(AppendTrack(*listTracks.toTypedArray()))


                // and after that, then we build the outputfile
                val container = DefaultMp4Builder().build(outputMovie)
                val fileChannel = RandomAccessFile(String.format(fileName), "rws").channel
                container.writeContainer(fileChannel)
                fileChannel.close()

                // and finally, we delete all the temporary files used before
                Commons.deleteFiles(audioFilePaths)



            } catch (e: IOException) {
                dismissProgress()
                Timber.d("Error building movie from chunks -> ${e.message}")
                runOnUiThread {
                    alert("There was an error merging your audio") {
                        okButton {  }
                    }.show()
                }

                // if this step fails, then we have to exit runnable
                return@Runnable
            }

            // all of the following code is intended to put the markers in the correct place
            val startPosMilliseconds =
                waveformView.pixelsToMillisecs(selectedMarker.startPos / NEW_WIDTH)
            val endPosMilliseconds =
                waveformView.pixelsToMillisecs(selectedMarker.endPos / NEW_WIDTH)
            val copiedLength = endPosMilliseconds - startPosMilliseconds
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


            //This line will remain the original marker when it is copied and pasted
            activity!!.runOnUiThread {
                addMarker(selectedMarker.startPos, selectedMarker.endPos, false, null) //TODO JJ
            }


        }).start()

    }

    /**
     * This method receives a path, start and endframes, and outputs a sound file in the specified path
     * from the start to the end frame.
     *
     * It returns the absolute path of the created file
     */
    private fun writeSoundTemporaryFile(
        path: String,
        startFrame: Int,
        endFrame: Int
    ): String {
        // we create the outputfile with the output path
        val file = File(path)
        val numFrames = endFrame - startFrame
        soundFile.WriteFile(
            file,
            startFrame,
            numFrames
        )
        return file.absolutePath
    }

    /**
     * This method receives a path, start and endframes, and outputs a sound file in the specified path
     * from the start to the end frame.
     *
     * It returns the absolute path of the created file
     */
    private fun writeSoundTemporaryFile(
        path: String,
        startSecond: Float,
        endSecond: Float
    ): String {
        // we create the outputfile with the output path
        val file = File(path)
        soundFile.WriteFile(
            file,
            startSecond,
            endSecond
        )
        return file.absolutePath
    }

    private fun deleteMarkedChunk() {
        if (markerSets == null || markerSets.size == 0 || isPlayingPreview || isPlaying || selectedMarker == null) {
            return
        }
        stepManager.addNewUndoStep(
            Step(System.currentTimeMillis(), fileName, recordingItem!!.timeStamps)
        )
        showProgress(getString(R.string.progress_please_wait))
        Thread(Runnable {
            var audioFilePaths = ArrayList<String>()
            for (i in 0..1) {
                val outPath =
                    activity!!.externalCacheDir!!.absolutePath + "/limor_record_chunk_" + i + ".m4a"
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
            fileName = activity!!.externalCacheDir!!.absolutePath + "/limor_record_" + System.currentTimeMillis() + "_edited.m4a"
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
                    intent.action!!.contains("BROADCAST_OPEN_HOW_TO_EDIT") -> {
                        openHowToEdit()
                    }
                    intent.action!!.contains("BROADCAST_OPEN_PUBLISH_SCREEN") -> {
                        openPublishFragment()
                    }
                    intent.action!!.contains("BROADCAST_RESTORE_INITIAL_RECORDING") -> {
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

            val path = context?.getExternalFilesDir(null)?.absolutePath
            val convertedFile = File(path, "/limorv2/" + System.currentTimeMillis() + ".wav")

            val commandToExecute1 = "-i " + recordingItem!!.filePath + " -f s16le -ac 2 -ar 16000 " + convertedFile
            val commandToExecute2 = "-i "+ recordingItem!!.filePath +" -acodec pcm_s16le -ac 2 -ar 16000 " + convertedFile
            val commandToExecute3 = "-i "+ recordingItem!!.filePath + " " + convertedFile

            val rc: Int = FFmpeg.execute(commandToExecute1)

            if (rc == RETURN_CODE_SUCCESS) {
                Timber.i("Command execution completed successfully.")

                val wavFileInCustomFolder = File(path, "/limorv2/" + System.currentTimeMillis() + ".wav")
                convertedFile.copyTo(wavFileInCustomFolder)

                try {
                    deleteFile(recordingItem!!.filePath)
                    deleteFile(convertedFile.absolutePath)
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

            } else if (rc == RETURN_CODE_CANCEL) {
                Timber.i("Command execution cancelled by user.")
                toast("Command execution cancelled by user.")
            } else {
                Timber.i(String.format("Command execution failed with rc=%d and the output below.", rc))
            }

        }else{
            draftViewModel.continueRecording = true
            recordingItem!!.timeStamps = timeStamps
            recordingItem!!.length = player.duration.toLong() //si modifico el audio y le doy atrás, el record sigue mostrando la duración del audio original, no el editado
            draftViewModel.durationOfLastAudio = player.duration.toLong()
            updateRecordingItem()

            findNavController().popBackStack()
        }
        handlePausePreview()
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
