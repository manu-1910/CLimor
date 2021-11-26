package com.limor.app.scenes.main_new.view

import android.content.DialogInterface
import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limor.app.R
import com.limor.app.audio.wav.waverecorder.WaveRecorder
import com.limor.app.audio.wav.waverecorder.calculateAmplitude
import com.limor.app.scenes.main.fragments.podcast.SimpleRecorder
import com.limor.app.scenes.utils.visualizer.PlayVisualizer
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.sheet_edit_preview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.sqrt

class BottomSheetEditPreview : BottomSheetDialogFragment() {

    private val castId: Int by lazy { requireArguments()[CAST_ID] as Int }
    private val castURL: String by lazy { requireArguments()[CAST_URL] as String }

    private lateinit var playVisualiser: PlayVisualizer
    private lateinit var rewindButton: ImageButton
    private lateinit var forwardButton: ImageButton
    private lateinit var playButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var destinationFile: File
    private lateinit var progressBar: ProgressBar

    val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.sheet_edit_preview, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViews(view)
        downloadCast()
        setClickListeners()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        } catch (e: Exception) {
            println("Exception stopping media player")
        }
    }

    private fun initialiseViews(view: View){
        playVisualiser = view.findViewById(R.id.playVisualizer)
        rewindButton = view.findViewById(R.id.rewindButton)
        forwardButton = view.findViewById(R.id.forwardButton)
        playButton = view.findViewById(R.id.playButton)
        saveButton = view.findViewById(R.id.saveButton)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setClickListeners(){
        rewindButton.setOnClickListener {

        }
        forwardButton.setOnClickListener {

        }
        playButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_play
                    )
                )
            } else {
                lifecycleScope.launch {
                    mediaPlayer.setDataSource(destinationFile.path)
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

                    val seekHandler: Handler = Handler(Looper.getMainLooper())
                    var seekUpdater: Runnable = object : Runnable {
                        override fun run() {
                            seekHandler.postDelayed(this, 100)
                            mediaPlayer.let {
                                if (it.isPlaying) {
                                    val currentPosition = it.currentPosition
                                    playVisualiser.updateTime(currentPosition.toLong(), true)
                                }
                            }
                        }
                    }

                    val mRecorder = WaveRecorder(destinationFile.path)
                    mRecorder.waveConfig.sampleRate = 44100
                    mRecorder.waveConfig.channels = AudioFormat.CHANNEL_IN_STEREO
                    mRecorder.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
                    val amps: List<Int> = loadAmps(destinationFile.path, mRecorder.bufferSize)

                    mediaPlayer.prepareAsync()
                    playVisualiser.visibility = View.VISIBLE

                    mediaPlayer.setOnCompletionListener {
                        playVisualiser.updateTime(mediaPlayer.duration.toLong(), false)
                        it.pause()
                        playButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_play
                            )
                        )
                    }
                    mediaPlayer.setOnPreparedListener {
                        it.start()
                        playButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_pause
                            )
                        )
                        seekHandler.post(seekUpdater)
                    }

                    playVisualiser.apply {
                        ampNormalizer = { sqrt(it.toFloat()).toInt() }
                    }
                    playVisualiser.setWaveForm(
                        amps,
                        mRecorder.tickDuration
                    )
                }
            }
        }
        saveButton.setOnClickListener {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            } catch (e: Exception) {
                println("Exception stopping media player")
            }
            dismiss()
        }
    }

    private fun showProgress(){
        progressBar.visibility = View.VISIBLE
        playButton.visibility = View.INVISIBLE
    }

    private fun hideProgress(){
        progressBar.visibility = View.GONE
        playButton.visibility = View.VISIBLE
    }

    private fun downloadCast(){
        if (castURL.isNotEmpty()) {
            val downloadDirectory =
                File(requireContext().getExternalFilesDir(null)?.absolutePath, "/limorv2/download/")
            if (!downloadDirectory.exists()) {
                val isDirectoryCreated = downloadDirectory.mkdir()
            }
            val fileName = castURL.substring(castURL.lastIndexOf("/") + 1)
            val finalPath: String = downloadDirectory.absolutePath + "/" + fileName
            destinationFile = File(finalPath)
            showProgress()
            doAsync {
                try{
                    URL(castURL).openStream().use { input ->
                        FileOutputStream(destinationFile).use { output ->
                            input.copyTo(output)
                            runOnUiThread {
                                hideProgress()
                            }
                        }
                    }
                }
                catch (e: Exception){
                    Log.d("sdvf",e.toString())
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadAmps(recordFile: String, bufferSize: Int): List<Int> = withContext(Dispatchers.IO) {
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

    companion object{
        const val CAST_ID = "CAST_ID"
        const val CAST_URL = "CAST_URL"
        const val TAG = "EDIT_PREVIEW_SHEET"
        fun newInstance(cast: CastUIModel): BottomSheetEditPreview {
            val instance = BottomSheetEditPreview()
            val args = Bundle()
            args.putInt(BottomSheetEditPreview.CAST_ID, cast.id)
            args.putString(BottomSheetEditPreview.CAST_URL, cast.audio?.url)
            instance.arguments = args
            return instance
        }
    }

}