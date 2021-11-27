package com.limor.app.scenes.main_new.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.limor.app.audio.wav.WavHelper
import com.limor.app.scenes.utils.waveform.WaveformFragment
import com.limor.app.uimodels.CastUIModel
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class EditPreviewFragment : WaveformFragment() {

    private val cast: CastUIModel by lazy {
        requireArguments().getParcelable(KEY_PODCAST)!!
    }

    private val audioFileName: String by lazy {
        val castURL = cast.audio?.url ?: ""
        val downloadDirectory =
            File(requireContext().getExternalFilesDir(null)?.absolutePath, "/limorv2/download/")
        if (!downloadDirectory.exists()) {
            val isDirectoryCreated = downloadDirectory.mkdirs()
        }
        val fileName = castURL.substring(castURL.lastIndexOf("/") + 1)
        downloadDirectory.absolutePath + "/" + fileName + ".wav"
    }

    override fun getFileName(): String {
        return audioFileName
    }

    override fun populateMarkers() {
        // TODO("Not yet implemented")
    }

    override fun shouldWaitForAudio(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        downloadCast()
        return view
    }

    private fun downloadCast() {
        val testURL = "https://limor-platform-development.s3-eu-west-1.amazonaws.com/podcast_audio_direct_upload/audioFile_163661872969286112_1636618729691.x-m4a"
        val castURL = testURL // cast.audio?.url ?: return
        val original = "$audioFileName.original"
        val file = File(original)

        doAsync {
            try {
                URL(castURL).openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                        WavHelper.convertToWavFile(requireContext(), original, audioFileName)
                        lifecycleScope.launch {
                            loadFromFile()
                        }
                    }
                }
            } catch (e: Exception) {
                // TODO show error ?
                Log.d("sdvf", e.toString())
            }
        }
    }

    companion object {
        val TAG = EditPreviewFragment::class.qualifiedName
        private const val KEY_PODCAST = "KEY_PODCAST"

        fun newInstance(cast: CastUIModel): EditPreviewFragment {
            return EditPreviewFragment().apply {
                arguments = bundleOf(KEY_PODCAST to cast)
            }
        }
    }
}