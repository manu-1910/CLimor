package com.limor.app.scenes.main_new.view

import android.app.Dialog
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limor.app.R
import com.limor.app.audio.wav.waverecorder.WaveRecorder
import com.limor.app.audio.wav.waverecorder.calculateAmplitude
import com.limor.app.databinding.SheetEditPreviewBinding
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
    private val castURL: String by lazy {
        "https://limor-platform-development.s3-eu-west-1.amazonaws.com/podcast_audio_direct_upload/audioFile_163661872969286112_1636618729691.x-m4a"
        // requireArguments()[CAST_URL] as String
    }

    private var _binding: SheetEditPreviewBinding? = null
    private val binding get() = _binding!!

    private var destinationFile: File? = null

    val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SheetEditPreviewBinding.inflate(inflater, container, false);
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadCast()
        setClickListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            if (dialog is BottomSheetDialog) {
                val behavior: BottomSheetBehavior<*> = dialog.behavior
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
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

    private fun setClickListeners() {
        binding.rewindButton.setOnClickListener {

        }
        binding.forwardButton.setOnClickListener {

        }
        binding.playButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                binding.playButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_play
                    )
                )
            } else {
                playAudio()
            }
        }
        binding.saveButton.setOnClickListener {
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

    private fun playAudio() {
        val audioFile = destinationFile ?: return

        mediaPlayer.setDataSource(audioFile.path)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        val seekHandler: Handler = Handler(Looper.getMainLooper())
        var seekUpdater: Runnable = object : Runnable {
            override fun run() {
                seekHandler.postDelayed(this, 100)
                mediaPlayer.let {
                    if (it.isPlaying) {
                        val currentPosition = it.currentPosition
                        binding.playVisualizer.updateTime(currentPosition.toLong(), true)
                    }
                }
            }
        }

        mediaPlayer.prepareAsync()

        mediaPlayer.setOnCompletionListener {
            binding.playVisualizer.updateTime(mediaPlayer.duration.toLong(), false)
            it.pause()
            binding.playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_play
                )
            )
        }
        mediaPlayer.setOnPreparedListener {
            it.start()
            binding.playButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pause
                )
            )
            seekHandler.post(seekUpdater)
        }
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.playButton.visibility = View.INVISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.playButton.visibility = View.VISIBLE
    }

    private fun downloadCast() {
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
                try {
                    URL(castURL).openStream().use { input ->
                        FileOutputStream(destinationFile).use { output ->
                            input.copyTo(output)
                            lifecycleScope.launch {
                                onFileDownloaded()
                            }
                        }
                    }
                } catch (e: Exception) {
                    // TODO show error ?
                    Log.d("sdvf", e.toString())
                }
            }
        }
    }

    private suspend fun onFileDownloaded() {
        val audioFile = destinationFile ?: return
        if (!audioFile.exists()) {
            // TODO show errors ?
            dismiss()
            return;
        }
        val mRecorder = WaveRecorder(audioFile.path)
        mRecorder.waveConfig.sampleRate = 44100
        mRecorder.waveConfig.channels = AudioFormat.CHANNEL_IN_STEREO
        mRecorder.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT

        lifecycleScope.launch(Dispatchers.IO) {
            val amps: List<Int> = loadAmps(audioFile.path, mRecorder.bufferSize)
            binding.playVisualizer.setWaveForm(
                amps,
                mRecorder.tickDuration
            )
        }

        binding.playVisualizer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            visibility = View.VISIBLE
        }

        hideProgress();
    }

    private fun loadAmps(recordFile: String, bufferSize: Int): List<Int> {
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
        return amps;
    }

    companion object {
        const val CAST_ID = "CAST_ID"
        const val CAST_URL = "CAST_URL"
        const val TAG = "EDIT_PREVIEW_SHEET"

        fun newInstance(cast: CastUIModel): BottomSheetEditPreview {
            return BottomSheetEditPreview().apply {
                arguments = Bundle().apply {
                    putInt(CAST_ID, cast.id)
                    putString(CAST_URL, cast.audio?.url)
                }
            }
        }
    }

}