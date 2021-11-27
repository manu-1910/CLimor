package com.limor.app.scenes.main_new.view

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.limor.app.scenes.utils.waveform.WaveformFragment
import com.limor.app.uimodels.CastUIModel
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import java.io.File
import java.net.URL

class EditPreviewFragment : WaveformFragment() {
    override fun getFileName(): String {
        TODO("Not yet implemented")
    }

    override fun populateMarkers() {
        TODO("Not yet implemented")
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