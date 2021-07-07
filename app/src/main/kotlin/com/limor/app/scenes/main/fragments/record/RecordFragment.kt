package com.limor.app.scenes.main.fragments.record

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.limor.app.App
import com.limor.app.R
import com.limor.app.audio.wav.WavHelper
import com.limor.app.audio.wav.waverecorder.WaveRecorder
import com.limor.app.audio.wav.waverecorder.calculateAmplitude
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.viewmodels.DraftViewModel
import com.limor.app.scenes.main.viewmodels.LocationsViewModel
import com.limor.app.scenes.utils.Commons
import com.limor.app.scenes.utils.CommonsKt.Companion.audioFileFormat
import com.limor.app.scenes.utils.CommonsKt.Companion.getDateTimeFormatted
import com.limor.app.scenes.utils.SpecialCharactersInputFilter
import com.limor.app.scenes.utils.location.MyLocation
import com.limor.app.scenes.utils.visualizer.RecordVisualizer
import com.limor.app.uimodels.UIDraft
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_cancel_draft.view.*
import kotlinx.android.synthetic.main.dialog_cancel_draft.view.saveButton
import kotlinx.android.synthetic.main.dialog_error_publish_cast.view.*
import kotlinx.android.synthetic.main.dialog_save_draft.view.*
import kotlinx.android.synthetic.main.fragment_record.*
import kotlinx.android.synthetic.main.sheet_more_draft.view.*
import kotlinx.android.synthetic.main.toolbar_default.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.sqrt


class RecordFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var draftViewModel: DraftViewModel
    private lateinit var locationsViewModel: LocationsViewModel
    private var isRecording = false
    private var isFirstTapRecording = true
    private var timeWhenStopped: Long = 0
    private var rootView: View? = null
    private var recordVisualizer: RecordVisualizer? = null
    private var mRecorder: WaveRecorder? = null
    private val insertDraftTrigger = PublishSubject.create<Unit>()
    private val deleteDraftTrigger = PublishSubject.create<Unit>()
    private var app: App? = null
    private var uiDraft: UIDraft? = null
    private lateinit var locationResult: MyLocation.LocationResult
    private var fileRecording = ""
    private var isAnimatingCountdown: Boolean = false
    private var needAnimatedCountDown = true
    private lateinit var handlerCountdown: Handler
    private var anythingToSave = false

    //Player record variables
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var tempFileName: String
    private var seekUpdater: Runnable
    private val seekHandler: Handler = Handler(Looper.getMainLooper())
    private var needToInitializeMediaPlayer = true
    private var playBackTime = 0L

    companion object {
        val TAG: String = RecordFragment::class.java.simpleName
        fun newInstance() = RecordFragment()
        private const val PERMISSION_ALL = 1
        private val PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


    init {
        seekUpdater = object : Runnable {
            override fun run() {
                seekHandler.postDelayed(this, 100)
                mediaPlayer.let {
                    if (it.isPlaying) {
                        val currentPosition = it.currentPosition
                        playVisualizer.updateTime(currentPosition.toLong(), true)
                        updatePlayBackLabel(currentPosition.toLong())
                        enableRewFwdButton(it)
                    }
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_record, container, false)
            recordVisualizer = rootView?.findViewById(R.id.graphVisualizer)
        }
        app = context?.applicationContext as App
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tempFileName = getNewFileName()
        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        rewButton.isEnabled = false
        ffwdButton.isEnabled = false

        bindViewModel()
//        deleteUnusedAudioFiles()
        initGui()
        listeners()
        initApiCallInsertDraft()
        initApiCallDeleteDraft()

        //Check Permissions
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL)
        }

    }


    private fun listeners() {
        enablePlayButton(false)
        // Next Button
        nextButton.onClick {
            if (mRecorder != null) {
                val resultStop = stopAudio()
                if (!resultStop) {
                    alert(getString(R.string.error_stopping_audio)) { okButton { } }
                    return@onClick
                }
            }

            // --------------- Read this carefully: -----------------
            // - filesArray will have size 1 when we first record an audio in the record fragment.
            //      I mean, if I just come to the RecordFragment and start a recording, this would
            //      be my only recording and my only file. This is this specific case.
            //
            // - filesArray will have size 2 and that second file won't exist yet when
            //      we came back to this fragment from edit or publish fragment but the user didn't
            //      record more audio, so the first audio file (the one that we received from the other
            //      fragment) is set, but the second one (the one that we should've created by recording audio) is not set.
            //
            // In both cases, we just have to set the only useful filePath and send it to the next fragment. Without merging anything.
            if (draftViewModel.filesArray.size == 1
                || draftViewModel.filesArray.size == 2 && !draftViewModel.filesArray[1].exists()
            ) {
                uiDraft?.filePath = draftViewModel.filesArray[0].absolutePath
                uiDraft?.editedFilePath = draftViewModel.filesArray[0].absolutePath
                insertDraftInRealm(uiDraft!!)

                val bundle = bundleOf("recordingItem" to uiDraft)
                //findNavController().navigate(R.id.action_record_fragment_to_record_publish, bundle)
                findNavController().navigate(R.id.action_record_fragment_to_record_publish, bundle)


                // This situation will happen when I came back from edit or publish fragment and then,
                // I have my previous recording that I received from the previous fragment (edit or publish),
                // and the new recording the user just recorded. Both files should be merged now.
            } else if (draftViewModel.filesArray.size == 2) {
                mergeFilesAndNavigateTo(R.id.action_record_fragment_to_record_publish)
            } else {
                val errorMessage =
                    "Error, you have -> ${draftViewModel.filesArray.size} items when merging audio"
                alert(errorMessage) { okButton { } }.show()
                Timber.e(errorMessage)
            }

        }

        // Record Button
        recordButton.onClick {

            playVisualizer.visibility = View.GONE
            c_meter.visibility = View.VISIBLE
            textPlaybackTime.visibility = View.INVISIBLE
            recordVisualizer?.visibility = View.VISIBLE
            updatePlayBackLabel(0)

            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                setPlayPauseButtonState(false)
            }
            if (isRecording) {
                pauseRecording()
                needToInitializeMediaPlayer = true
                enablePlayButton(true)
            } else {
                if (needAnimatedCountDown) {
                    showCountdownAnimation {
                        startRecording()
                    }
                } else {
                    stopCountdownAnimationAndStartRecordingInstantly()
                }
                enablePlayButton(false)
            }
        }

        playButton.onClick {
            if (needToInitializeMediaPlayer) {
                val recordingFile = File(fileRecording)
                val copiedFile = File(tempFileName)
                lifecycleScope.launch {
                    if (recordingFile.exists()) {
                        val amps: List<Int>
                        //We returned from drafts screen
                        if (uiDraft?.draftParent != null) {
                            val tempFilePath = getNewFileName()
                            val tempFile = File(tempFilePath)
                            recordingFile.copyTo(tempFile, true)
                            mRecorder?.writeHeaders(tempFilePath)
                            WavHelper.combineWaveFile(
                                draftViewModel.filesArray[0].absolutePath,
                                tempFile.absolutePath,
                                copiedFile.absolutePath
                            )
                            amps = loadAmps(copiedFile.absolutePath, mRecorder?.bufferSize!!)
                            if (tempFile.exists()) tempFile.delete()
                            //It's a new recording
                        } else {
                            recordingFile.copyTo(copiedFile, true)
                            mRecorder?.writeHeaders(tempFileName)
                            amps = loadAmps(copiedFile.absolutePath, mRecorder?.bufferSize!!)
                        }
                        playVisualizer.apply {
                            ampNormalizer = { sqrt(it.toFloat()).toInt() }
                            onSeeking = {
                                mediaPlayer.seekTo(it.toInt())
                                updatePlayBackLabel(it)
                                enableRewFwdButton(mediaPlayer)
                            }
                        }
                        playVisualizer.setWaveForm(
                            amps,
                            mRecorder!!.tickDuration
                        )
                        configureMediaPlayer(copiedFile)
                    }
                }
            } else {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    setPlayPauseButtonState(false)
                } else {
                    mediaPlayer.start()
                    setPlayPauseButtonState(true)
                }
            }
        }

        ffwdButton.onClick {
            try {
                val nextPosition = mediaPlayer.currentPosition + 5000
                if (nextPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(nextPosition)
                else if (mediaPlayer.currentPosition < mediaPlayer.duration)
                    mediaPlayer.seekTo(mediaPlayer.duration)
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo forward overflow")
            }
        }

        rewButton.onClick {
            try {
                mediaPlayer.seekTo(mediaPlayer.currentPosition - 5000)
            } catch (e: Exception) {
                Timber.d("mediaPlayer.seekTo rewind overflow")
            }
        }

    }

    private fun updatePlayBackLabel(playBack: Long) {
        playBackTime = playBack
        textPlaybackTime.text = Commons.getLengthFromEpochForPlayer(playBackTime)
    }

    private fun setPlayPauseButtonState(isPlaying: Boolean) {
        if (isPlaying)
            playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pause
                )
            )
        else
            playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.play_button
                )
            )
    }


    private fun configureMediaPlayer(audio: File) {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setDataSource(audio.absolutePath)
        mediaPlayer.setOnCompletionListener {
            setPlayPauseButtonState(false)
            playVisualizer.updateTime(mediaPlayer.duration.toLong(), false)
            updatePlayBackLabel(mediaPlayer.duration.toLong())
            enableRewFwdButton(it)
            mediaPlayer.pause()

        }
        mediaPlayer.setOnPreparedListener {
            it.start()
            updatePlayBackLabel(0)
            seekHandler.post(seekUpdater)
            setPlayPauseButtonState(true)
        }
        mediaPlayer.prepareAsync()
        needToInitializeMediaPlayer = false
        playVisualizer.visibility = View.VISIBLE
        recordVisualizer!!.visibility = View.GONE
        c_meter.visibility = View.INVISIBLE
        textPlaybackTime.visibility = View.VISIBLE
    }

    private fun enablePlayButton(isEnabled: Boolean) {
        playButton.isEnabled = isEnabled
        //rewButton.isEnabled = isEnabled
        //ffwdButton.isEnabled = isEnabled
    }

    private fun enableRewFwdButton(mediaPlayer: MediaPlayer) {
        rewButton.isEnabled = mediaPlayer.currentPosition > 0
        ffwdButton.isEnabled = mediaPlayer.currentPosition < mediaPlayer.duration
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as RecordActivity).initSlideBehaviour()
        requestForLocation()
        requireActivity().registerReceiver(
            lowBatteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_LOW)
        )
        requireActivity().registerReceiver(
            incomeCallReceiver,
            IntentFilter("android.intent.action.PHONE_STATE")
        )
        // this means that we come from another fragment to continue recording
        draftViewModel.uiDraft?.let {

            isFirstTapRecording = true
            anythingToSave = true

            //Put the seconds counter with the length of the draftitem
            updateChronoTimerFromDraft()

            // this means that we come from another fragment and that this is an autosave from
            // another draft, so we just assign the received draft to the current uiDraft
            if (it.draftParent != null) {

                uiDraft = draftViewModel.uiDraft


                // this means that we come from another fragment but we are not yet an autosave.
                // we could come from draft list fragment, so we have to make an autosave from this
                // or we could come from another fragment but we are a new recording that just navigated
                // to another fragment to edit that new recording and came back, for example
            } else {


                // this means that we come from drafts list fragment, so we have to create a new autosave
                // draft and assign the parent as the uiDraft received from this fragment
                if (!it.isNewRecording) {
                    uiDraft = it.copy()
                    uiDraft?.id = System.currentTimeMillis()
                    uiDraft?.title = getString(R.string.autosave)
                    if (it.filePath != null) {
                        val fileFromParent = File(it.filePath!!)
                        if (fileFromParent.exists()) {
                            val copiedFilePath = getNewFileName()
                            val copiedFile = fileFromParent.copyTo(File(copiedFilePath), true)
                            if (copiedFile.exists()) {
                                uiDraft?.filePath = copiedFilePath
                                draftViewModel.filesArray.clear()
                                draftViewModel.filesArray.add(copiedFile)
                            } else {
                                alert(getString(R.string.error_copying_file))
                            }
                        } else {
                            createNewAutosavedDraft()
                            uiDraft?.isNewRecording = true
                        }
                    } else {
                        alert(getString(R.string.file_not_received)).show()
                    }

                    uiDraft?.draftParent = it
                    loadDraftToRecordingWave()


                    // this means that we are a new recording that just navigated to another fragment to edit
                    // the audio for example and then we came back to the record fragment, so we just assign the received
                    // draft to the local uiDraft of this fragment
                } else {
                    uiDraft = draftViewModel.uiDraft
                    needToInitializeMediaPlayer = true
                    enablePlayButton(true)
                }
            }




            changeToEditToolbar()
            updateRecordButton()


            // this means that we come to this fragment to record something new
        } ?: run {

            createNewAutosavedDraft()
            uiDraft?.isNewRecording = true
        }
    }

    private fun loadDraftToRecordingWave() {
        if (!File(uiDraft?.draftParent!!.filePath!!).exists()) return
        lifecycleScope.launch {
            //Create dummy recorder to get wave parameters
            val recorder = WaveRecorder("")
            val amps: List<Int> =
                loadAmps(uiDraft?.draftParent!!.filePath!!, recorder.bufferSize!!)
            amps.forEach { amp ->
                recordVisualizer?.addAmp(
                    amp, recorder.tickDuration
                )
            }
            needToInitializeMediaPlayer = true
            enablePlayButton(true)
        }

    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(lowBatteryReceiver)
        requireActivity().unregisterReceiver(incomeCallReceiver)
    }


    private fun onBackPressed() {
        // if the drafts is null, it means that we haven't even recorded anything, so we just exit the activity
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            setPlayPauseButtonState(false)
        }
        if (!anythingToSave) {
            uiDraft = null
            activity?.finish()
            // if we do have recorded anything...
        } else {
            // this means that we are in this fragment recording some audio for the first time
            if (uiDraft?.isNewRecording == true) {

                // let's show the dialog to show if they want to save the current recorded audio or discard it
                showSaveDraftDialog()

                // if we are here, this means that we are in this fragment because we come from
                // another fragment that sent us some draft to continue recording it
            } else showCancelDraftDialog()
        }
    }

    private fun showCancelDraftDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_cancel_draft, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog: AlertDialog = dialogBuilder.create()

        dialogView.overwriteButton.setOnClickListener {
            dialog.dismiss()
            val resultStop = stopAudio()
            if (!resultStop) {
                alert(getString(R.string.error_stopping_audio)) { okButton { } }
                return@setOnClickListener
            }

            mergeFilesIfNecessary {
                uiDraft?.title = uiDraft?.draftParent?.title
                val parentToDelete = uiDraft?.draftParent?.copy()
                uiDraft?.draftParent =
                    null // this is necessary because if we don't do it
                // then when we retreive it from realm we may think that
                // this is still a children draft and it may not
                insertDraftInRealm(uiDraft!!)
                if (parentToDelete != null)
                    deleteDraftInRealm(parentToDelete)
                activity?.finish()
            }
        }

        dialogView.saveButton.setOnClickListener {
            dialog.dismiss()
            val resultStop = stopAudio()
            if (!resultStop) {
                alert(getString(R.string.error_stopping_audio)) { okButton { } }
                return@setOnClickListener
            }

            showSaveDraftAlert { title ->
                mergeFilesIfNecessary {
                    uiDraft?.title = title
                    uiDraft?.draftParent =
                        null // this is necessary because if we don't do it
                    // then when we retreive it from realm we may think that
                    // this is still a children draft and it may not
                    insertDraftInRealm(uiDraft!!)
                    activity?.finish()
                }
            }
        }

        dialogView.discardButton.setOnClickListener {
            dialog.dismiss()
            deleteDraftInRealm(uiDraft!!)
            activity?.finish()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset)
            show()
        }
    }


    private fun showSaveDraftDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_save_draft, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(true)
        val dialog: AlertDialog = dialogBuilder.create()

        dialogView.noButton.setOnClickListener {
            deleteDraftInRealm(uiDraft!!)
            activity?.finish()
        }

        dialogView.saveButton.setOnClickListener {
            dialog.dismiss()
            showSaveDraftAlert()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset)
            show()
        }
    }


    private fun createNewAutosavedDraft(): UIDraft {
        val auxDraft = UIDraft()
        auxDraft.id = System.currentTimeMillis()
        auxDraft.title = getString(R.string.autosaved_draft)
        auxDraft.date = getDateTimeFormatted()
        uiDraft = auxDraft
        return auxDraft
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

    private fun updateChronoTimerFromDraft() {
        draftViewModel.uiDraft?.filePath?.let {
            val file = File(it)
            if (file.exists()) {
                val uri: Uri = Uri.parse(it)
                val mmr = MediaMetadataRetriever()
                try {
                    mmr.setDataSource(context, uri)
                    val durationStr =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val currentDurationInMillis = durationStr!!.toInt()
                    //val currentDurationInSecondsFloat: Double = currentDurationInMillis / 1000.0
                    //val durationFloatRoundedUp = kotlin.math.ceil(currentDurationInSecondsFloat)
                    // val durationMillisRoundedUp = (durationFloatRoundedUp * 1000).toInt()
                    c_meter.base = SystemClock.elapsedRealtime() - currentDurationInMillis
                    timeWhenStopped = c_meter.base - SystemClock.elapsedRealtime()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.d("Couldn't read metadata and duration. This could be because the file you're trying to access is corrupted")
                }
            }
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_record)

        //Toolbar Left
        btnToolbarLeft.text = getString(R.string.btn_cancel)
        btnToolbarLeft.onClick {
            activity?.finish()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_drafts)
        btnToolbarRight.onClick {
            uiDraft = null // this is necessary
            findNavController().navigate(R.id.action_record_fragment_to_record_drafts)
        }
    }

    private fun showSaveDraftAlert(onPositiveClicked: (title: String) -> Unit) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_with_edittext, null)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelButton)
        val editText = dialogLayout.findViewById<TextInputEditText>(R.id.editText)
        val titleText = dialogLayout.findViewById<TextView>(R.id.textTitle)

        titleText.text = requireContext().getString(R.string.save_draft_dialog_title)
        editText.filters = arrayOf(SpecialCharactersInputFilter())
        editText.doOnTextChanged { text, start, before, count ->
            positiveButton.isEnabled = count > 0
        }

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)

        val dialog: AlertDialog = dialogBuilder.create()

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

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset);
            show()
        }
    }


    private fun showSaveDraftAlert() {
        showSaveDraftAlert {
            uiDraft?.title = it
            uiDraft?.isNewRecording = false
            uiDraft?.date = getDateTimeFormatted()

            //Inserting in Realm
            insertDraftInRealm(uiDraft!!)

            toast(getString(R.string.draft_inserted))

            activity?.finish()
        }
    }


    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProvider(it, viewModelFactory)
                .get(DraftViewModel::class.java)

           /* locationsViewModel = ViewModelProvider(it, viewModelFactory)
                .get(LocationsViewModel::class.java)*/
        }
    }


    private fun hideToolbarButtons() {
        btnToolbarRight.visibility = View.GONE
        btnToolbarLeft.visibility = View.GONE
    }


    private fun showToolbarButtons() {
        btnToolbarRight.visibility = View.VISIBLE
        btnToolbarLeft.visibility = View.VISIBLE
    }


    private fun clearToolBarButtons() {
        //Toolbar Left
        btnToolbarLeft.background = null
        btnToolbarLeft.text = ""

        //Toolbar Right
        btnToolbarRight.background = null
        btnToolbarRight.text = ""
    }


    private fun changeToEditToolbar() {

        clearToolBarButtons()

        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_record)

        //Toolbar Left
        btnToolbarLeft.text = getString(R.string.btn_cancel)
        btnToolbarLeft.onClick {
            onBackPressed()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_edit)
        btnToolbarRight.onClick {
            onEditClicked()
        }
    }

    private fun onEditClicked() {
        val resultStopAudio = stopAudio()
        if (!resultStopAudio) {
            alert(getString(R.string.error_stopping_audio)) { okButton { } }
            return
        }
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            setPlayPauseButtonState(false)
        }
        // --------------- Read this carefully: -----------------
        // - filesArray will have size 1 when we first record an audio in the record fragment.
        //      I mean, if I just come to the RecordFragment and start a recording, this would
        //      be my only recording and my only file. This is this specific case.
        //
        // - filesArray will have size 2 and that second file won't exist yet when
        //      we came back to this fragment from edit or publish fragment but the user didn't
        //      record more audio, so the first audio file (the one that we received from the other
        //      fragment) is set, but the second one (the one that we should've created by recording audio) is not set.
        //
        // In both cases, we just have to set the only useful filePath and send it to the next fragment. Without merging anything.
        if (draftViewModel.filesArray.size == 1
            || draftViewModel.filesArray.size == 2 && !draftViewModel.filesArray[1].exists()
        ) {
            uiDraft?.filePath = draftViewModel.filesArray[0].absolutePath
            uiDraft?.editedFilePath = draftViewModel.filesArray[0].absolutePath
            insertDraftInRealm(uiDraft!!)

            val bundle = bundleOf("recordingItem" to uiDraft)
            //findNavController().navigate(R.id.action_record_fragment_to_record_publish, bundle)
            findNavController().navigate(R.id.action_record_fragment_to_record_edit, bundle)


            // This situation will happen when I came back from edit or publish fragment and then,
            // I have my previous recording that I received from the previous fragment (edit or publish),
            // and the new recording the user just recorded. Both files should be merged now.
        } else if (draftViewModel.filesArray.size == 2) {

            mergeFilesAndNavigateTo(R.id.action_record_fragment_to_record_edit)
        } else {
            val errorMessage =
                "Error, you have -> ${draftViewModel.filesArray.size} items when merging audio"
            alert(errorMessage) { okButton { } }.show()
            Timber.e(errorMessage)
        }
    }


    private fun mergeFilesAndNavigateTo(navigationAction: Int) {
        doAsync {
            val finalAudio = File(
                context?.getExternalFilesDir(null)?.absolutePath,
                "/limorv2/" + System.currentTimeMillis() + audioFileFormat
            )
            if (WavHelper.combineWaveFile(
                    draftViewModel.filesArray[0].absolutePath,
                    draftViewModel.filesArray[1].absolutePath,
                    finalAudio.absolutePath
                )
            ) {
                // let's delete the old files, we don't need them anymore
                draftViewModel.filesArray.forEach {
                    it.delete()
                }

                // let's empty the filesArray
                draftViewModel.filesArray.clear()
                // and let's add the combined audio, that is now the ONLY audio
                draftViewModel.filesArray.add(finalAudio)

                //The recording item will be passed to EditFragment as Argument inside a bundle
                uiDraft?.filePath = finalAudio.absolutePath

                insertDraftInRealm(uiDraft!!)



                uiThread {
                    //Go to Publish fragment
                    // TODO: Jose -> maybe we don't need any bundle if we are using viewmodels
                    if (navigationAction > 0) {
                        val bundle = bundleOf("recordingItem" to uiDraft)
                        findNavController().navigate(
                            navigationAction,
                            bundle
                        )
                    } else {
                        activity?.finish()
                    }
                }
            } else {
                uiThread {
                    activity?.alert(getString(R.string.error_merging_file)) {
                        okButton { }
                    }?.show()
                }
            }

        }
    }

    private fun mergeFilesIfNecessary(callback: () -> Unit) {
        doAsync {
            val finalAudio = File(
                context?.getExternalFilesDir(null)?.absolutePath,
                "/limorv2/" + System.currentTimeMillis() + audioFileFormat
            )

            // this means that there is only one file and there is no need to merge anything
            if (draftViewModel.filesArray.size == 1) {
                uiDraft?.filePath = draftViewModel.filesArray[0].absolutePath
                uiThread { callback() }

                // this means that there are actually two files to merge
            } else if (draftViewModel.filesArray.size == 2) {
                if (WavHelper.combineWaveFile(
                        draftViewModel.filesArray[0].absolutePath,
                        draftViewModel.filesArray[1].absolutePath,
                        finalAudio.absolutePath
                    )
                ) {
                    // let's delete the old files, we don't need them anymore
                    draftViewModel.filesArray.forEach {
                        it.delete()
                    }

                    // let's empty the filesArray
                    draftViewModel.filesArray.clear()
                    // and let's add the combined audio, that is now the ONLY audio
                    draftViewModel.filesArray.add(finalAudio)

                    uiDraft?.filePath = finalAudio.absolutePath

                    uiThread {
                        callback()
                    }
                } else {
                    uiThread {
                        activity?.alert(getString(R.string.error_merging_file)) {
                            okButton { }
                        }?.show()
                    }
                }
            } else {
                uiThread {
                    alert("There are three files in the list, it shouldn't") { }.show()
                }
            }
        }
    }

    private fun initGui() {
        configureToolbar()
        // Disable next button
        nextButton.isEnabled = false
    }


    private fun resetRecorderWithNewFile() {
        fileRecording = getNewFileName()
        mRecorder = WaveRecorder(fileRecording)
        mRecorder?.waveConfig?.sampleRate = 44100
        mRecorder?.waveConfig?.channels = AudioFormat.CHANNEL_IN_STEREO
        mRecorder?.waveConfig?.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        recordVisualizer?.ampNormalizer = { sqrt(it.toFloat()).toInt() }
        mRecorder?.onAmplitudeListener = {
            runOnUiThread {
                if (isRecording) {
                    if (it != 0) {
                        recordVisualizer?.addAmp(it, mRecorder!!.tickDuration)
                    }
                }
            }
        }

    }


    private fun getNewFileName(): String {
        // Note: this is not the audio file name, it's a directory.
        val recordingDirectory =
            File(context?.getExternalFilesDir(null)?.absolutePath + "/limorv2/")
        if (!recordingDirectory.exists()) {
            recordingDirectory.mkdir()
        }


        return recordingDirectory.absolutePath + "/" + System.currentTimeMillis() + audioFileFormat
    }

    private fun stopCountdownAnimationAndStartRecordingInstantly() {
        handlerCountdown.removeCallbacksAndMessages(null)
        layCountdownAnimation.visibility = View.GONE
        isAnimatingCountdown = false
        startRecording()
    }

    private fun showCountdownAnimation(callback: () -> Unit) {
        needAnimatedCountDown = false
        isAnimatingCountdown = true
        val animatorCountDown = ObjectAnimator.ofPropertyValuesHolder(
            layCountdownAnimation,
            PropertyValuesHolder.ofFloat("scaleX", 1.1f),
            PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        )

        animatorCountDown.duration = 50
        animatorCountDown.repeatCount = 1
        animatorCountDown.interpolator = FastOutSlowInInterpolator()
        animatorCountDown.repeatMode = ObjectAnimator.REVERSE

        var count = 3
        handlerCountdown = Handler()
        val runnableAnim = object : Runnable {
            override fun run() {
                if (count > 0) {
                    animatorCountDown.start()
                    handlerCountdown.postDelayed(this, 1000)
                    count--
                    tvCountdown.text = count.toString()
                } else {
                    layCountdownAnimation.visibility = View.GONE
                    isAnimatingCountdown = false
                    callback()
                }

            }
        }
        layCountdownAnimation.visibility = View.VISIBLE
        tvCountdown.text = "3"
        handlerCountdown.postDelayed(runnableAnim, 1000)
    }

    private fun pauseRecording() {
        println("RECORD --> PAUSE")
        mRecorder?.pauseRecording()
        //Stop the chronometer and anotate the time when it is stopped
        timeWhenStopped = c_meter.base - SystemClock.elapsedRealtime()
        isRecording = false
        //Change toolbar
        updateRecordButton()
        changeToEditToolbar()
        showToolbarButtons()

        //Stop timer
        c_meter.stop()
    }

    private fun updateRecordButton() {
        if (isRecording) {
            nextButton.isEnabled = false
            recordButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.pause_red_btn
            )
        } else {
            recordButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.record_red_btn)
            // Enable next button
            nextButton.isEnabled = true
        }
    }


    private fun startRecording() {

        //Check if all permissions are granted, if not, request again
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            try {
                requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {

            isRecording = true

            updateRecordButton()
            if (isFirstTapRecording) {
                isFirstTapRecording = false
                println("RECORD --> START")
                resetRecorderWithNewFile()
                mRecorder?.startRecording()
                //This is the recorded audio file saved in storage
                val fileChosen = File(fileRecording)
                uiDraft?.filePath = fileChosen.absolutePath
                draftViewModel.filesArray.add(fileChosen)
                insertDraftInRealm(uiDraft!!)
                val tickPerBar = 100 / mRecorder!!.tickDuration
                val barDuration = tickPerBar * mRecorder!!.tickDuration
                Timber.d("Audio Bar duration" + barDuration)

            } else {
                println("RECORD --> RESUME")
                mRecorder?.resumeRecording()
            }
            anythingToSave = true

            //Update times in digital clock
            c_meter.base = SystemClock.elapsedRealtime() + timeWhenStopped
            c_meter.start()

            hideToolbarButtons()
        }

    }


    private fun stopAudio(): Boolean {
        println("RECORD --> STOP")
        try {
            mRecorder?.stopRecording()
            isRecording = false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        updateRecordButton()
        val fileChosen = File(fileRecording)
        if (!draftViewModel.filesArray.contains(fileChosen)) {
            draftViewModel.filesArray.add(fileChosen)
        }


        //Change toolbar
        changeToEditToolbar()
        showToolbarButtons()

        //Stop timer
        c_meter.stop()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()

        try {
            mRecorder?.stopRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        recordVisualizer?.clearAnimation()
        recordVisualizer?.clear()

        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
        }
        seekHandler.removeCallbacksAndMessages(null)
        if (::handlerCountdown.isInitialized) {
            handlerCountdown.removeCallbacksAndMessages(null)
            layCountdownAnimation.visibility = View.GONE
            isAnimatingCountdown = false
        }
    }

    private fun initApiCallInsertDraft() {
        val output = draftViewModel.insertDraftRealm(
            DraftViewModel.InputInsert(
                insertDraftTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            if (it) {
                //toast(getString(R.string.draft_inserted))
                println("Draft inserted succesfully")
            } else {
                //toast(getString(R.string.draft_not_inserted))
                println("Error inserting draft in Realm")
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.draft_not_inserted_error))
        })
    }


    private fun initApiCallDeleteDraft() {
        val output = draftViewModel.deleteDraftRealm(
            DraftViewModel.InputDelete(
                deleteDraftTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            if (it) {
                //toast(getString(R.string.draft_deleted))
                println("Draft deleted in realm")
            } else {
                //toast(getString(R.string.draft_not_deleted))
                println("Error deleting draft in realm")
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.draft_not_inserted_error))
        })
    }


    @Deprecated("")
    private fun insertDraftInRealm2(item: UIDraft, isNew: Boolean) {
        //Model to save in Realm

        if (isNew) { //Create new Item
            val newItem = UIDraft()
            newItem.id = System.currentTimeMillis()
            newItem.filePath = item.filePath

            if (item.title.isNullOrEmpty()) {
                newItem.title = getString(R.string.autosaved_draft)
            }
            if (item.date.isNullOrEmpty()) {
                newItem.date = getDateTimeFormatted()
            }

            draftViewModel.uiDraft = newItem
        } else {
            //Update existing Item
            if (item.title.isNullOrEmpty()) {
                item.title = getString(R.string.autosaved_draft)
            }
            if (item.date.isNullOrEmpty()) {
                item.date = getDateTimeFormatted()
            }

            draftViewModel.uiDraft = item
        }


        //Insert in Realm
        insertDraftTrigger.onNext(Unit)
    }

    private fun insertDraftInRealm(item: UIDraft) {
        draftViewModel.uiDraft = item
        insertDraftTrigger.onNext(Unit)
    }


    private fun deleteDraftInRealm(item: UIDraft) {
        draftViewModel.uiDraft = item

        //Insert in Realm
        deleteDraftTrigger.onNext(Unit)
    }


    private fun requestForLocation() {

        val isPermissionsNotGranted = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        if (isPermissionsNotGranted) return

        locationResult = object : MyLocation.LocationResult() {
            override fun gotLocation(location: Location?) {
                //Got the location!
                println("Location received: " + location?.latitude + "," + location?.longitude)
                context?.let {
                    val geoCoder = Geocoder(it, Locale.getDefault()) //it is Geocoder
                    try {
                        val address: List<Address> = geoCoder.getFromLocation(
                            location!!.latitude,
                            location.longitude,
                            1
                        )
                        when {
                            address[0].locality != null -> {
                                locationsViewModel.uiLocationsRequest.term = address[0].locality
                                println("Location received " + address[0].locality)
                            }
                            address[0].adminArea != null -> {
                                locationsViewModel.uiLocationsRequest.term = address[0].adminArea
                                println("Location received " + address[0].adminArea)
                            }
                            address[0].thoroughfare != null -> {
                                locationsViewModel.uiLocationsRequest.term = address[0].thoroughfare
                                println("Location received " + address[0].thoroughfare)
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        val myLocation = MyLocation()
        myLocation.getLocation(requireContext(), locationResult)
    }

    private val lowBatteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            if (isRecording) {
                pauseRecording()
            }
            showLowBatteryDialog()
        }
    }

    private fun showLowBatteryDialog() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_battery_low, null)

        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)

        val dialog: AlertDialog = dialogBuilder.create()

        dialogLayout.okButton.onClick {
            dialog.dismiss()
        }

        val inset = InsetDrawable(ColorDrawable(Color.TRANSPARENT), 20)

        dialog.apply {
            window?.setBackgroundDrawable(inset);
            show()
        }
    }

    private val incomeCallReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.intent.action.PHONE_STATE") {
                val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                if (phoneState == TelephonyManager.EXTRA_STATE_RINGING) {
                    // The handset is not lifted, the phone rings
                    Timber.d("Phone calls ring")
                    val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                } else if (phoneState == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                    // Phone is in a call (dial an outgoing call / talk)
                } else if (phoneState == TelephonyManager.EXTRA_STATE_IDLE) {
                    // The phone is in standby mode - this event occurs at the end of a conversation or situation, "refused to pick up the phone and dropped the call."
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadAmps(recordFile: String, bufferSize: Int): List<Int> = withContext(IO) {
        val amps = mutableListOf<Int>()
        val buffer = ByteArray(bufferSize)
        File(recordFile).inputStream().use {
            it.skip(44.toLong())

            var count = it.read(buffer)
            while (count > 0) {
                amps.add(buffer.calculateAmplitude())
                count = it.read(buffer)
            }
        }
        amps
    }


    // TODO: Jose -> review this method
    // be careful with this method. It doesn't seem to work as expected.
    // I mean, it does delete files, but maybe it deletes files that we don't want to be deleted.
    private fun deleteUnusedAudioFiles() {
        // let's fill a paths list with all the file paths that should be in the storage
        draftViewModel.loadDraftRealm()?.observe(this@RecordFragment, Observer<List<UIDraft>> {
            val paths = ArrayList<String>()
            it.forEach { draft ->
                draft.filePath?.let { path ->
                    paths.add(path)
                }
            }
            // and now, let's delete all zombie files that are not linked to any draft anymore but,
            // for some reason, they were not deleted
            val audioDirectory = File(context?.getExternalFilesDir(null)?.absolutePath, "/limorv2/")
            if (audioDirectory.exists() && audioDirectory.isDirectory) {
                val files = audioDirectory.listFiles()
                files?.forEach { file ->
                    if (!paths.contains(file.absolutePath))
                        file.delete()
                }
            }
        })


        // and now, let's clean cache too
        context?.externalCacheDir?.absolutePath?.let {
            val cacheDirectory = File(it)
            if (cacheDirectory.exists() && cacheDirectory.isDirectory) {
                val cacheFiles = cacheDirectory.listFiles()
                cacheFiles?.forEach { file ->
                    file.delete()
                }
            }
        }
    }


}

