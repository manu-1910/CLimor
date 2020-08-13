package io.square1.limor.scenes.main.fragments.record

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.getColor
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.main.viewmodels.DraftViewModel
import io.square1.limor.scenes.utils.Commons.*
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.scenes.utils.VisualizerView
import io.square1.limor.uimodels.UIDraft
import kotlinx.android.synthetic.main.fragment_record.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import java.io.File
import javax.inject.Inject
import kotlin.collections.ArrayList


class RecordFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var draftViewModel: DraftViewModel
    private var isRecording = false
    private var isFirstTapRecording = true
    private var timeWhenStopped: Long = 0
    private var rootView: View? = null
    private var voiceGraph : VisualizerView? = null
    private lateinit var filesArray: ArrayList<File>
    private var audioFile1: String = ""
    private var audioFile2: String = ""
    private var handler = Handler()
    private lateinit var updater: Runnable
    private lateinit var mRecorder : AMRAudioRecorder
    private val PERMISSION_ALL = 1
    private var PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val insertDraftTrigger = PublishSubject.create<Unit>()
    var app: App? = null
    var recordingItem: UIDraft? = null


    companion object {
        val TAG: String = RecordFragment::class.java.simpleName
        fun newInstance() = RecordFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_record, container, false)

            voiceGraph = rootView?.findViewById(R.id.graphVisualizer)
            filesArray = ArrayList()
        }
        app = context?.applicationContext as App
        return rootView
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        configureToolbar()
        audioSetup()
        listeners()
        insertDraft()

        //Check Permissions
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            requestPermissions( PERMISSIONS, PERMISSION_ALL)
        }
    }


    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
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
            findNavController().navigate(R.id.action_record_fragment_to_record_drafts)
        }
    }


    private fun showSaveDraftAlert(view: View) {
        val dialog = AlertDialog.Builder(context)
        val inflater = layoutInflater
        dialog.setTitle(getString(R.string.save_draft_dialog_title))
        val dialogLayout = inflater.inflate(R.layout.dialog_with_edittext, null)
        val positiveButton= dialogLayout.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelButton)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        dialog.setView(dialogLayout)
        dialog.setCancelable(false)
        val ad: AlertDialog = dialog.show()

        positiveButton.onClick {

            recordingItem?.title = editText.text.toString()
            recordingItem?.caption = CommonsKt.getDateTimeFormatted()

            //Inserting in Realm
            draftViewModel.uiDraft = recordingItem!!
            insertDraftTrigger.onNext(Unit)

            findNavController().navigate(R.id.action_record_fragment_to_record_drafts)
            ad.dismiss()
        }

        cancelButton.onClick {
            ad.dismiss()
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
        }
    }


    private fun hideToolbarButtons(){
        btnToolbarRight.visibility = View.GONE
        btnToolbarLeft.visibility = View.GONE
    }


    private fun showToolbarButtons() {
        btnToolbarRight.visibility = View.VISIBLE
        btnToolbarLeft.visibility = View.VISIBLE
    }


    private fun clearToolBarButtons(){
        //Toolbar Left
        btnToolbarLeft.background = null
        btnToolbarLeft.text = ""

        //Toolbar Right
        btnToolbarRight.background = null
        btnToolbarRight.text = ""
    }


    private fun changeToEditToolbar(){

        clearToolBarButtons()

        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_record)

        //Toolbar Left
        btnToolbarLeft.text = getString(R.string.btn_cancel)
        btnToolbarLeft.onClick { //Here the user has start recording
            alert(getString(R.string.alert_cancel_record_descr), getString(R.string.alert_cancel_record_title)) {
                positiveButton(getString(R.string.alert_cancel_record_save)){
                    it.dismiss()
                    showSaveDraftAlert(view!!)
                }
                negativeButton(getString(R.string.alert_cancel_record_do_not_save)){
                    activity?.finish()
                }
            }.show()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_edit)
        btnToolbarRight.onClick {
            //waveRecorder?.stopRecording() //TODO JJ

            isRecording = false
            stopAudio()

            //Merge audios and delete all them except the Audio Merged
            if (filesArray.size == 1) {
                var bundle = bundleOf("recordingItem" to recordingItem)
                //findNavController().navigate(R.id.action_record_fragment_to_record_publish, bundle)
                findNavController().navigate(R.id.action_record_fragment_to_record_edit, bundle)
            }else{
                doAsync {
                    val finalAudio = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/" + System.currentTimeMillis() + ".aac")
                    recordingItem?.filePath = finalAudio.absolutePath
                    if(mergeAmrAudioFiles(filesArray, finalAudio.absolutePath)){
                        //Delete all files when they are merged
                        if(deleteFilesInArray(filesArray)){
                            filesArray.clear()
                            filesArray.add(finalAudio)
                        }else{
                            println("Fail deleting audios")
                        }
                    }else{
                        println("Fail merge audios")
                    }
                    uiThread {
                        //Go to Publish fragment
                        var bundle = bundleOf("recordingItem" to recordingItem)
                        findNavController().navigate(R.id.action_record_fragment_to_record_edit, bundle)
                    }
                }
            }
        }
    }


    private fun audioSetup() {

        // Note: this is not the audio file name, it's a directory.
        val recordingDirectory = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/")
        if(!recordingDirectory.exists()){
            recordingDirectory.mkdir()
        }

        if (recordingItem != null){
            changeToEditToolbar()
        }else{
            mRecorder = AMRAudioRecorder(recordingDirectory.absolutePath)

            // Disable next button
            //nextButton.background = getDrawable(requireContext(), R.drawable.bg_round_grey_ripple)
            nextButton.background = getDrawable(requireContext(), R.drawable.bg_round_grey_ripple)
            nextButton.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            nextButton.isEnabled = false
            nextButton.visibility = View.GONE
        }

    }

    private fun resetAudioSetup() {

        // Note: this is not the audio file name, it's a directory.
        val recordingDirectory = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/")
        if(!recordingDirectory.exists()){
            recordingDirectory.mkdir()
        }

        mRecorder = AMRAudioRecorder(recordingDirectory.absolutePath)
    }

    private fun listeners(){

        // Next Button
        nextButton.onClick {

            //Stop the recorder
            try {
                mRecorder.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            isFirstTapRecording = true
            resetAudioSetup()

            //Stop chronometer
            c_meter.base = SystemClock.elapsedRealtime()
            timeWhenStopped = 0

            //Add the file to the files array
            getLastModified()?.let {
                if(!filesArray.contains(it)){
                    filesArray.add(it)
                }
            }
            printFilesArray()

            //Merge audios and delete all them except the Audio Merged
            if (filesArray.size == 1) {
                var bundle = bundleOf("recordingItem" to recordingItem)
                findNavController().navigate(R.id.action_record_fragment_to_record_publish, bundle)
            }else{
                doAsync {
                    val finalAudio = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/" + System.currentTimeMillis() + ".aac")

                    if(mergeAmrAudioFiles(filesArray, finalAudio.absolutePath)){
                        recordingItem?.filePath = finalAudio.absolutePath
                        //Delete all files when they are merged
                        if(deleteFilesInArray(filesArray)){
                            filesArray.clear()
                            filesArray.add(finalAudio)
                        }else{
                            println("Fail deleting audios")
                        }
                    }else{
                        println("Fail merge audios")
                    }
                    uiThread {
                        //Go to Publish fragment
                        var bundle = bundleOf("recordingItem" to recordingItem)
                        findNavController().navigate(R.id.action_record_fragment_to_record_publish, bundle)
                    }
                }
            }
        }

        // Record Button
        recordButton.onClick {
            if (isRecording){
                stopAudio()
            }else{
                recordAudio()
            }
        }

        // Listener on amplitudes changes to update the Audio Visualizer
        updater = object : Runnable {
            override fun run() {
                handler.postDelayed(this, 40)
                val maxAmplitude: Int = mRecorder.maxAmplitude
                if (maxAmplitude != 0) {
                    if(isRecording){
                        voiceGraph?.addAmplitude(maxAmplitude.toFloat())
                        voiceGraph?.invalidate() // refresh the Visualizer
                    }
                }
            }
        }

    }


    private fun recordAudio() {

        //Check if all permissions are granted, if not, request again
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            try {
                requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{

            isRecording = true

            // Disable next button
            nextButton.background = getDrawable(requireContext(), R.drawable.bg_round_grey_ripple)
            nextButton.isEnabled = false
            nextButton.visibility = View.VISIBLE

            recordButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.pause_red)

            if(isFirstTapRecording){
                isFirstTapRecording = false
                mRecorder.start()

                //This is the recorded audio file saved in storage
                val fileChosen : File? = getLastModified()

                if(recordingItem != null){
                    recordingItem?.filePath = fileChosen?.absolutePath
                    recordingItem?.editedFilePath = fileChosen?.absolutePath
                }else{
                    recordingItem = UIDraft()
                    recordingItem?.id = System.currentTimeMillis()/1000000
                    recordingItem?.filePath = fileChosen?.absolutePath
                    recordingItem?.editedFilePath = fileChosen?.absolutePath
                }


                handler.post(updater)
            }else{
                mRecorder.resume()
            }

            //Start timer
            c_meter.base = SystemClock.elapsedRealtime() + timeWhenStopped
            c_meter.start()

            hideToolbarButtons()
        }

    }



    private fun stopAudio() {

        recordButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.record_red)

        // Enable next button
        nextButton.visibility = View.VISIBLE
        nextButton.background = requireContext().getDrawable(R.drawable.bg_round_yellow_ripple)
        nextButton.isEnabled = true

        if (isRecording) {
            mRecorder.pause()
            //Stop the chronometer and anotate the time when it is stopped
            timeWhenStopped = c_meter.base - SystemClock.elapsedRealtime()
            isRecording = false
        } else {
            mRecorder.stop()

            //Add the file to the files array
            getLastModified()?.let {
                if(!filesArray.contains(it)){
                    filesArray.add(it)
                }
            }
            printFilesArray()

            //Model to save in Realm
            if(recordingItem?.title.isNullOrEmpty()){
                recordingItem?.title = getString(R.string.autosaved_draft)
            }
            recordingItem?.caption = CommonsKt.getDateTimeFormatted()
            recordingItem?.length = c_meter.base
            recordingItem?.time = System.currentTimeMillis() / 1000
            //Inserting in Realm
            draftViewModel.uiDraft = recordingItem!!
            insertDraftTrigger.onNext(Unit)
        }


        //Change toolbar
        changeToEditToolbar()
        showToolbarButtons()

        //Stop timer
        c_meter.stop()
    }



    override fun onDestroy() {
        super.onDestroy()

        //TODO JJ Test this
        try {
            mRecorder.stop()
        } catch (e: Exception) {
        }

        try {
            handler.removeCallbacks(updater)
        } catch (e: Exception) {
        }

        voiceGraph?.clearAnimation()
        voiceGraph?.clear()
    }



    private fun insertDraft() {
        val output = draftViewModel.insertDraftRealm(
            DraftViewModel.InputInsert(
                insertDraftTrigger
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


    private fun getLastModified(): File? {
        val directoryFilePath = Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/"
        val directory = File(directoryFilePath)
        val files = directory.listFiles { obj: File -> obj.isFile }
        var lastModifiedTime = Long.MIN_VALUE
        var chosenFile: File? = null
        if (files != null) {
            for (file in files) {
                if (file.lastModified() > lastModifiedTime) {
                    chosenFile = file
                    lastModifiedTime = file.lastModified()
                }
            }
        }
        return chosenFile
    }


    private fun printFilesArray(){ //TODO JJ Delete this method when this flow is tested
        println("start-------------------------------------------")
        for(file in filesArray){
            println("File in filesArray "+file.name)
        }
        println("end---------------------------------------------")
    }




}

