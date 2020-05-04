package io.square1.limor.scenes.main.fragments.record

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.scenes.utils.VisualizerView
import io.square1.limor.uimodels.UIRecordingItem
import kotlinx.android.synthetic.main.fragment_record.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.toast
import java.io.File


class RecordFragment : BaseFragment() {


    private var isRecording = false
    private var isFirstTapRecording = true
    private var timeWhenStopped: Long = 0
    private var rootView: View? = null
    private var voiceGraph : VisualizerView? = null
    var app: App? = null
    var recordingItem: UIRecordingItem? = null
    private var handler = Handler()
    private lateinit var updater: Runnable
    private lateinit var mRecorder : AMRAudioRecorder

    private val PERMISSION_ALL = 1
    private var PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    companion object {
        val TAG: String = RecordFragment::class.java.simpleName
        fun newInstance() = RecordFragment()
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

    //override fun onCreateView(
    //    inflater: LayoutInflater, container: ViewGroup?,
    //    savedInstanceState: Bundle?
    //): View? {
    //    return inflater.inflate(R.layout.fragment_record, container, false)
    //}


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModel()
        configureToolbar()
        audioSetup()
        listeners()

        //Check Permissions
        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            requestPermissions( PERMISSIONS, PERMISSION_ALL)
        }
    }




    private fun bindViewModel() {
        /* activity?.let { fragmentActivity ->
             mainViewModel = ViewModelProviders
                 .of(fragmentActivity, viewModelFactory)
                 .get(MainViewModel::class.java)
         }*/
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
            toast("Cast name is " + editText.text.toString()) //TODO delete this when drafts screen implemented
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
        btnToolbarLeft.onClick {
            activity?.finish()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_edit)
        btnToolbarRight.onClick {
            //waveRecorder?.stopRecording() //TODO JJ

            isRecording = false
            stopAudio()

            var bundle = bundleOf("recordingItem" to recordingItem)
            findNavController().navigate(R.id.action_record_fragment_to_record_edit, bundle)
        }
    }


    private fun audioSetup() {




//        /**
//         * This path points to application cache directory.
//         * you could change it based on your usage
//         */
//        audioFilePath = Environment.getExternalStorageDirectory()?.absolutePath + "/audioFile.wav"
//
//        if(waveRecorder == null){
//            waveRecorder = WaveRecorder(audioFilePath)
//            waveRecorder?.waveConfig?.sampleRate = 16000
//            waveRecorder?.waveConfig?.channels = AudioFormat.CHANNEL_IN_STEREO
//            waveRecorder?.waveConfig?.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
//            waveRecorder?.noiseSuppressorActive = true
//
//            // Disable next button
//            nextButton.background = getDrawable(requireContext(), R.drawable.bg_round_grey_ripple)
//            nextButton.isEnabled = false
//        }else{
//            // Enable next button
//            nextButton.background = requireContext().getDrawable(R.drawable.bg_round_yellow_ripple)
//            nextButton.isEnabled = true
//        }


        // Note: this is not the audio file name, it's a directory.
        val recordingDirectory = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/")
        if(!recordingDirectory.exists()){
            recordingDirectory.mkdir()
        }

        mRecorder = AMRAudioRecorder(recordingDirectory.absolutePath)

        // Disable next button
        nextButton.background = getDrawable(requireContext(), R.drawable.bg_round_grey_ripple)
        nextButton.isEnabled = false

    }



    private fun listeners(){

        // Next Button
        nextButton.onClick {
            //waveRecorder?.stopRecording()
            mRecorder.stop() //TODO JJ


            //Stop chronometer
            c_meter.base = SystemClock.elapsedRealtime()
            timeWhenStopped = 0

            //Go tu Publish fragment
            findNavController().navigate(R.id.action_record_fragment_to_record_publish)
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

            recordButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.pause_red)

            if(isFirstTapRecording){
                isFirstTapRecording = false
                mRecorder.start()
                recordingItem = UIRecordingItem()
                handler.post(updater);
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
        nextButton.background = requireContext().getDrawable(R.drawable.bg_round_yellow_ripple)
        nextButton.isEnabled = true

        if (isRecording) {

            mRecorder.pause()

            //Stop the chronometer and anotate the time when it is stopped
            timeWhenStopped = c_meter.base - SystemClock.elapsedRealtime()
//
//            //recordingItem?.filePath = audioFilePath
//            recordingItem?.filePath =  mRecorder.audioFilePath
//            //recordingItem?.editedFilePath = audioFilePath
//            recordingItem?.editedFilePath = mRecorder.audioFilePath
//            recordingItem?.length = timeWhenStopped
//            recordingItem?.title = "Autosave"
//            recordingItem?.time = System.currentTimeMillis() / 1000

            isRecording = false

        } else {

            mRecorder.stop()

            //TODO JJ only for testing purposes
            //recordingItem?.filePath = mRecorder.audioFilePath
            //recordingItem?.editedFilePath = mRecorder.audioFilePath
            recordingItem?.filePath = Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/1587987477835.amr"
            recordingItem?.editedFilePath = Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/1587987477835.amr"

            recordingItem?.length = c_meter.base
            recordingItem?.title = "Autosave"
            recordingItem?.time = System.currentTimeMillis() / 1000
        }

        //Change toolbar
        changeToEditToolbar()
        showToolbarButtons()

        //Stop timer
        c_meter.stop()
    }



    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(updater);

        voiceGraph?.clearAnimation()
        voiceGraph?.clear()
    }


}

