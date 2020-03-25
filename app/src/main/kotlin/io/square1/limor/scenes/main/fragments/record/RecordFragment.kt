package io.square1.limor.scenes.main.fragments.record

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.github.squti.androidwaverecorder.WaveRecorder
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_record.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.runOnUiThread


class RecordFragment : BaseFragment() {


    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var waveRecorder: WaveRecorder

    private lateinit var audioFilePath: String
    private var isRecording = false
    private var isFirstTapRecording = true
    private var timeWhenStopped: Long = 0

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

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
        btnToolbarLeft.text = "Cancel"
        btnToolbarLeft.onClick {
            activity?.finish()
        }

        //Toolbar Right
        btnToolbarRight.text = getString(R.string.btn_drafts)
        btnToolbarRight.onClick {
            try {
                findNavController().navigate(R.id.action_record_fragment_to_record_drafts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

        //val titleToolbar = findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle?.text = getString(R.string.title_record)

        //val btnToolbarLeft = findViewById<ImageButton>(R.id.btnToolbarLeft)
        btnToolbarLeft.text = "Cancel"
        btnToolbarLeft.onClick {
            try {
                activity?.finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        //val btnToolbarRigth = findViewById<Button>(R.id.btnToolbarRight)
        btnToolbarRight.text = "Edit"
        btnToolbarRight.onClick {
            try {
                findNavController().navigate(R.id.action_record_fragment_to_record_edit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun audioSetup() {

        nextButton.isEnabled = false

        mediaPlayer = MediaPlayer() //TODO this is temporaly

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

        // Next Button
        nextButton.onClick {
            //toast("Do something with the audio file")
            //changeToEditToolbar()
            //playAudio()

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
        waveRecorder.onAmplitudeListener = {
            //Log.i(TAG, "Amplitude : $it")
            runOnUiThread {
                //Log.i(TAG, "runOnUiThread")
                if(isRecording){
                    graphVisualizer.addAmplitude(it.toFloat())
                    graphVisualizer.invalidate() // refresh the Visualizer
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
                waveRecorder.startRecording()
            }else{
                waveRecorder.resumeRecording()
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
            waveRecorder.pauseRecording()

            //Stop the chronometer and anotate the time when it is stopped
            timeWhenStopped = c_meter.base - SystemClock.elapsedRealtime()

            isRecording = false
        } else {
            mediaPlayer.release()
            //mediaPlayer = null

            waveRecorder.stopRecording()
        }


        //Change toolbar
        changeToEditToolbar()
        showToolbarButtons()

        //Stop timer
        c_meter.stop()
    }



    private fun playAudio() {
        //TODO This functionallity will not exists, only for test purposes (delete it)
        nextButton.isEnabled = false

        try {
            waveRecorder.stopRecording()

            graphVisualizer?.clearAnimation()
            graphVisualizer?.clear()

            //Stop chronometer
            c_meter.base = SystemClock.elapsedRealtime()
            timeWhenStopped = 0

            //mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioFilePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    override fun onDestroy() {
        super.onDestroy()

        if (mediaPlayer != null) {
            try {
                mediaPlayer.release()
                //mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }


}

