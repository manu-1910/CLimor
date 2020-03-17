package io.square1.limor.scenes.main.fragments

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.getColor
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_record.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast


class RecordFragment : BaseFragment() {


    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var isFirstTapRecording = true
    private val RECORD_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 102
    // create the Handler for visualizer update
    private var handler: Handler = Handler()

    companion object {
        val TAG: String = RecordFragment::class.java.simpleName
        fun newInstance() = RecordFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
        audioSetup()
        listeners()

    }



    private fun bindViewModel() {
        /* activity?.let { fragmentActivity ->
             mainViewModel = ViewModelProviders
                 .of(fragmentActivity, viewModelFactory)
                 .get(MainViewModel::class.java)
         }*/
    }



    private fun listeners(){
        playButton.onClick { playAudio() }
        nextButton.onClick {
            try {
                mediaRecorder?.release()
                mediaRecorder = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
            toast("Do something with the audio file")
        }
        recordButton.onClick {
            if (isRecording){
                stopAudio()
            }else{
                recordAudio()
            }
        }
    }


    private fun audioSetup() {

        nextButton.isEnabled = false

        audioFilePath = Environment.getExternalStorageDirectory().absolutePath + "/myaudio.3gp"

        val PERMISSION_ALL = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        requestPermissions(PERMISSIONS, PERMISSION_ALL)
    }



    private fun recordAudio() {
        isRecording = true

        // Disable next button
        nextButton.background = context?.getDrawable(R.drawable.bg_round_grey_ripple)
        nextButton.isEnabled = false

        recordButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.pause_red)

        if(isFirstTapRecording){

            isFirstTapRecording = false

            try {
                mediaRecorder = MediaRecorder()
                mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mediaRecorder?.setOutputFile(audioFilePath)
                mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mediaRecorder?.prepare()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            mediaRecorder?.start()

        }else{

            if (mediaRecorder != null) { //MediaRecorder has not been stopped (released)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        mediaRecorder?.resume()
                    }else{
                        mediaRecorder?.start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }else{
                mediaRecorder?.start()
            }

        }

        handler.post(updateVisualizer)

        //Start timer
        c_meter.start()
    }



    private fun stopAudio() {

        recordButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.record_red)

        // Enable next button
        nextButton.background = context?.getDrawable(R.drawable.bg_round_yellow_ripple)
        nextButton.isEnabled = true

        if (isRecording) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
            }else{
                mediaRecorder?.stop()
            }
            //mediaRecorder?.release()
            //mediaRecorder = null

            isRecording = false

        } else {
            //mediaPlayer?.release()
            //mediaPlayer = null
        }


        handler.removeCallbacks(updateVisualizer)
        //graphVisualizer?.clearAnimation()
        graphVisualizer?.clear()

        //Stop timer
        c_meter.stop()
    }



    private fun playAudio() {
        //TODO This functionallity will not exists, only for test purposes (delete it)
        nextButton.isEnabled = false

        try {
            mediaRecorder?.release()
            mediaRecorder = null

            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(audioFilePath)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun requestPermission(permissionType: String, requestCode: Int) {
        val permission = ContextCompat.checkSelfPermission(requireContext(), permissionType)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(context as Activity, arrayOf(permissionType), requestCode
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    recordButton.isEnabled = false
                    toast(getText(R.string.record_permission_required))
                } else {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_REQUEST_CODE)
                }
                return
            }
            STORAGE_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    recordButton.isEnabled = false
                    toast(getText(R.string.storage_permission_required))
                }
                return
            }
        }
    }


    // updates the visualizer every 40 milliseconds
    private var updateVisualizer: Runnable = object : Runnable {
        override fun run() {

            if(mediaRecorder?.maxAmplitude != null){
                graphVisualizer.addAmplitude(mediaRecorder!!.maxAmplitude.toFloat())
            }else {
                graphVisualizer.addAmplitude(1.toFloat())
            }
            graphVisualizer.invalidate() // refresh the VisualizerView
            // update each 40 milliseconds
            handler.postDelayed(this, 40)
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        //Stop graphVisualizer
        handler.removeCallbacks(updateVisualizer)

        //Stop mediarecorder
        mediaRecorder?.release()
        mediaRecorder = null

        mediaPlayer?.release()
        mediaPlayer = null
    }


}

