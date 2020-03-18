package io.square1.limor.scenes.main.fragments

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.github.squti.androidwaverecorder.WaveRecorder
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_record.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast


class RecordFragment : BaseFragment() {


    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioFilePath: String
    private var isRecording = false
    private var isFirstTapRecording = true
    private val RECORD_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 102
    private lateinit var waveRecorder: WaveRecorder

    val PERMISSION_ALL = 1
    val PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    companion object {
        val TAG: String = RecordFragment::class.java.simpleName
        fun newInstance() = RecordFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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



    private fun audioSetup() {

        requestPermissions(PERMISSIONS, PERMISSION_ALL)

        nextButton.isEnabled = false

        /**
         * This path points to application cache directory.
         * you could change it based on your usage
         */
        audioFilePath = Environment.getExternalStorageDirectory()?.absolutePath + "/audioFile.wav"

        waveRecorder = WaveRecorder(audioFilePath)
        waveRecorder.waveConfig.sampleRate = 44100
        waveRecorder.waveConfig.channels = AudioFormat.CHANNEL_IN_STEREO
        waveRecorder.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        waveRecorder.noiseSuppressorActive = true

    }



    private fun listeners(){
        // Play Button TODO this will be removed
        playButton.onClick { playAudio() }

        // Next Button
        nextButton.onClick {
            toast("Do something with the audio file")
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
        waveRecorder.onAmplitudeListener = {
            Log.i(TAG, "Amplitude : $it")


            runOnUiThread {
                Log.i(TAG, "runOnUiThread")
                if(isRecording){
                    graphVisualizer.addAmplitude(it.toFloat())
                    graphVisualizer.invalidate() // refresh the Visualizer
                }

            }
        }

    }




    private fun recordAudio() {
        isRecording = true

        // Disable next button
        nextButton.background = context?.getDrawable(R.drawable.bg_round_grey_ripple)
        nextButton.isEnabled = false

        recordButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.pause_red)

        if(isFirstTapRecording){
            isFirstTapRecording = false
            waveRecorder.startRecording()
        }else{
            waveRecorder.resumeRecording()
        }

        //Start timer
        c_meter.start()
    }



    private fun stopAudio() {

        recordButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.record_red)

        // Enable next button
        nextButton.background = context?.getDrawable(R.drawable.bg_round_yellow_ripple)
        nextButton.isEnabled = true

        if (isRecording) {
            waveRecorder.pauseRecording()
            isRecording = false
        } else {
            mediaPlayer.release()
            //mediaPlayer = null

            waveRecorder.stopRecording()
        }


        //handler.removeCallbacks(updateVisualizer)
        //graphVisualizer?.clearAnimation()
        //graphVisualizer?.clear()

        //Stop timer
        c_meter.stop()
    }



    private fun playAudio() {
        //TODO This functionallity will not exists, only for test purposes (delete it)
        nextButton.isEnabled = false

        try {
            //mediaRecorder?.release()
            //mediaRecorder = null
            waveRecorder.stopRecording()
            graphVisualizer?.clearAnimation()
            graphVisualizer?.clear()

            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioFilePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
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


    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer.release()
        //mediaPlayer = null
    }


}

