package com.limor.app.scenes.main.fragments.record

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.media.AudioFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.limor.app.App
import com.limor.app.R
import com.limor.app.audio.wav.WavHelper
import com.limor.app.audio.wav.waverecorder.WaveRecorder
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.viewmodels.DraftViewModel
import com.limor.app.scenes.main.viewmodels.LocationsViewModel
import com.limor.app.scenes.utils.CommonsKt.Companion.audioFileFormat
import com.limor.app.scenes.utils.CommonsKt.Companion.getDateTimeFormatted
import com.limor.app.scenes.utils.VisualizerView
import com.limor.app.scenes.utils.location.MyLocation
import com.limor.app.uimodels.UIDraft
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_record.*
import kotlinx.android.synthetic.main.toolbar_default.*
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


class RecordFragment : BaseFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var draftViewModel: DraftViewModel
    private lateinit var locationsViewModel: LocationsViewModel
    private var isRecording = false
    private var isFirstTapRecording = true
    private var timeWhenStopped: Long = 0
    private var rootView: View? = null
    private var voiceGraph: VisualizerView? = null
    private var mRecorder: WaveRecorder? = null
    private val insertDraftTrigger = PublishSubject.create<Unit>()
    private val deleteDraftTrigger = PublishSubject.create<Unit>()
    private var app: App? = null
    private var uiDraft: UIDraft? = null
    private lateinit var locationResult: MyLocation.LocationResult
    private var fileRecording = ""
    private var isAnimatingCountdown: Boolean = false
    private lateinit var handlerCountdown : Handler
    private var isNewRecording = false
    private var anythingRecorded = false


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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_record, container, false)
            voiceGraph = rootView?.findViewById(R.id.graphVisualizer)
        }
        app = context?.applicationContext as App
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)


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


    override fun onResume() {
        super.onResume()

        requestForLocation()


        // this means that we come from another fragment to continue recording
        draftViewModel.uiDraft?.let {

            isFirstTapRecording = true
            isNewRecording = false

            //Put the seconds counter with the length of the draftitem
            updateChronoTimerFromDraft()

            // this means that we come from edit or publish fragment, so we just assign the
            // received draft to the current uiDraft
            if(it.draftParent != null) {
                uiDraft = draftViewModel.uiDraft


                // this means that we come from drafts list fragment, so we have to create a new autosave
                // draft and assign the parent as the uiDraft received from this fragment
            } else {
                createNewAutosavedDraft()
                if(it.filePath != null) {
                    val fileFromParent = File(it.filePath!!)
                    if(fileFromParent.exists()) {
                        draftViewModel.filesArray.clear()
                        draftViewModel.filesArray.add(fileFromParent)
                    } else {
                        alert("Error, the file that we received from the previous fragment doesn't exist").show()
                    }
                } else {
                    alert("Error, we didn't receive any audio from the previous fragment").show()
                }

                uiDraft?.draftParent = it
            }




            changeToEditToolbar()
            updateRecordButton()


            // this means that we come to this fragment to record something new
        } ?: run {

            createNewAutosavedDraft()
            isNewRecording = true
        }
    }



    private fun onBackPressed() {
        // if the drafts is null, it means that we haven't even recorded anything, so we just exit the activity
        if (!anythingRecorded) {
            uiDraft = null
            activity?.finish()


            // if we do have recorded anything...
        } else {


            // this means that we are in this fragment recording some audio for the first time
            if(isNewRecording) {

                // let's show the dialog to show if they want to save the current recorded audio or discard it
                alert(
                    getString(R.string.alert_cancel_record_descr),
                    getString(R.string.alert_cancel_record_title)
                ) {

                    // if OK, let's save it
                    positiveButton(getString(R.string.alert_cancel_record_save)) {
                        it.dismiss()
                        showSaveDraftAlert()
                    }

                    // if CANCEL, then let's delete the temporary draft that it's now created
                    negativeButton(getString(R.string.alert_cancel_record_do_not_save)) {
                        deleteDraftInRealm(uiDraft!!)
                        activity?.finish()
                    }
                }.show()


                // if we are here, this means that we are in this fragment because we come from
                // another fragment that sent us some draft to continue recording it
            } else {

                // this situation means that we have actually recorded something new after coming to this fragment
                // so we have to ask the user if he wants to keep the new changes or discard
                if(draftViewModel.filesArray.size == 2 && draftViewModel.filesArray[1].exists()) {
                    alert(getString(R.string.do_you_want_to_save_changes)) {
                        positiveButton(getString(R.string.save)) {
                            mergeFilesAndCloseActivity()
                        }


                        negativeButton(getString(R.string.discard)) {
                            activity?.finish()
                        }
                    }.show()


                    // this means that we received a draft from another fragment, but we didn't record anything new in it
                    // so we don't have to save, we just close the activity
                } else {
                    activity?.finish()
                }
            }
        }
    }



    private fun createNewAutosavedDraft() : UIDraft {
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
                    val currentDurationInMillis = durationStr.toInt()
                    val currentDurationInSecondsFloat: Double = currentDurationInMillis / 1000.0
                    val durationFloatRoundedUp = kotlin.math.ceil(currentDurationInSecondsFloat)
                    val durationMillisRoundedUp = (durationFloatRoundedUp * 1000).toInt()
                    c_meter.base = SystemClock.elapsedRealtime() - durationMillisRoundedUp
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


    private fun showSaveDraftAlert() {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        dialogBuilder.setTitle(getString(R.string.save_draft_dialog_title))
        val dialogLayout = inflater.inflate(R.layout.dialog_with_edittext, null)
        val positiveButton = dialogLayout.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelButton)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)
        dialogBuilder.setView(dialogLayout)
        dialogBuilder.setCancelable(false)
        val dialog: AlertDialog = dialogBuilder.show()

        positiveButton.onClick {

            uiDraft?.title = editText.text.toString()
            uiDraft?.date = getDateTimeFormatted()

            //Inserting in Realm
            insertDraftInRealm(uiDraft!!)

            toast(getString(R.string.draft_inserted))

            activity?.finish()

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


    private fun bindViewModel() {
        activity?.let {
            draftViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(DraftViewModel::class.java)

            locationsViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(LocationsViewModel::class.java)
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

    private fun mergeFilesAndCloseActivity() {
        mergeFilesAndNavigateTo(-1)
    }

    private fun mergeFilesAndNavigateTo(navigationAction : Int) {
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
                    if(navigationAction > 0) {
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


    private fun initGui() {
        configureToolbar()
        // Disable next button
        nextButton.background = getDrawable(requireContext(), R.drawable.bg_round_grey_ripple)
        nextButton.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        nextButton.isEnabled = false
        nextButton.visibility = View.GONE
    }


    private fun resetRecorderWithNewFile() {
        fileRecording = getNewFileName()
        mRecorder = WaveRecorder(fileRecording)
        mRecorder?.waveConfig?.sampleRate = 44100
        mRecorder?.waveConfig?.channels = AudioFormat.CHANNEL_IN_STEREO
        mRecorder?.waveConfig?.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        mRecorder?.onAmplitudeListener = {
            runOnUiThread {
                if (isRecording) {
                    if (it != 0) {
                        voiceGraph?.addAmplitude(it.toFloat())
                        voiceGraph?.invalidate() // refresh the Visualizer
                    }
                }
            }
        }
    }


    private fun getNewFileName() : String {
        // Note: this is not the audio file name, it's a directory.
        val recordingDirectory =
            File(context?.getExternalFilesDir(null)?.absolutePath + "/limorv2/")
        if (!recordingDirectory.exists()) {
            recordingDirectory.mkdir()
        }


        return recordingDirectory.absolutePath + "/" + System.currentTimeMillis() + audioFileFormat
    }


    private fun listeners() {

        // Next Button
        nextButton.onClick {
            if(mRecorder != null) {
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
            if (isRecording) {
                pauseRecording()
            } else {
                if(!isAnimatingCountdown) {
                    showCountdownAnimation {
                        startRecording()
                    }
                } else {
                    stopCountdownAnimationAndStartRecordingInstantly()
                }
            }
        }

    }


    private fun stopCountdownAnimationAndStartRecordingInstantly() {
        handlerCountdown.removeCallbacksAndMessages(null)
        layCountdownAnimation.visibility = View.GONE
        isAnimatingCountdown = false
        startRecording()
    }

    private fun showCountdownAnimation(callback: () -> Unit) {
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
                if(count > 0) {
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
            nextButton.background = getDrawable(requireContext(), R.drawable.bg_round_grey_ripple)
            nextButton.isEnabled = false
            nextButton.visibility = View.VISIBLE

            recordButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.pause_red
            )
        } else {
            recordButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.record_red)

            // Enable next button
            nextButton.visibility = View.VISIBLE
            nextButton.background = requireContext().getDrawable(R.drawable.bg_round_yellow_ripple)
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

            } else {
                println("RECORD --> RESUME")
                mRecorder?.resumeRecording()
            }
            anythingRecorded = true

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


//    override fun onDestroy() {
//        super.onDestroy()
//
//        try {
//            mRecorder.stopRecording()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        voiceGraph?.clearAnimation()
//        voiceGraph?.clear()
//    }


    override fun onDestroyView() {
        super.onDestroyView()

        try {
            mRecorder?.stopRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        voiceGraph?.clearAnimation()
        voiceGraph?.clear()
    }

    private fun initApiCallInsertDraft() {
        val output = draftViewModel.insertDraftRealm(
            DraftViewModel.InputInsert(
                insertDraftTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it) {
                //toast(getString(R.string.draft_inserted))
                println("Draft inserted succesfully")
            } else {
                //toast(getString(R.string.draft_not_inserted))
                println("Error inserting draft in Realm")
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
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

        output.response.observe(this, Observer {
            if (it) {
                //toast(getString(R.string.draft_deleted))
                println("Draft deleted in realm")
            } else {
                //toast(getString(R.string.draft_not_deleted))
                println("Error deleting draft in realm")
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            toast(getString(R.string.draft_not_inserted_error))
        })
    }


//    private fun getLastModified(): File? {
//        val directoryFilePath = context?.getExternalFilesDir(null)?.absolutePath + "/limorv2/"
//        val directory = File(directoryFilePath)
//        val files = directory.listFiles { obj: File -> obj.isFile }
//        var lastModifiedTime = Long.MIN_VALUE
//        var chosenFile: File? = null
//        if (files != null) {
//            for (file in files) {
//                if (file.lastModified() > lastModifiedTime) {
//                    chosenFile = file
//                    lastModifiedTime = file.lastModified()
//                }
//            }
//        }
//        return chosenFile
//    }


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

        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }


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
        myLocation.getLocation(context!!, locationResult)
    }

}

