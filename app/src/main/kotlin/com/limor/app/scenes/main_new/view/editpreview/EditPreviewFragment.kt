package com.limor.app.scenes.main_new.view.editpreview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.limor.app.R
import com.limor.app.audio.wav.WavHelper
import com.limor.app.scenes.utils.waveform.WaveformFragment
import com.limor.app.uimodels.CastUIModel
import kotlinx.android.synthetic.main.fragment_waveform.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject

class EditPreviewFragment : WaveformFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: UpdatePreviewViewModel by viewModels { viewModelFactory }

    private var progressWrapper: View? = null
    private var progressTitle: TextView? = null
    private var progressBar: ProgressBar? = null
    private var processingProgressBar: ProgressBar? = null

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
        val startPixels = waveformView.adjustedMillisecsToPixels(0)
        val endPixels = waveformView.adjustedMillisecsToPixels(5000)
        addMarker(startPixels, endPixels, false, R.color.white)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO stop audio, stop processing etc
    }

    private fun setUI(root: View?) {
        val view = root ?: return

        nextButtonEdit.isEnabled = false

        progressWrapper = view.findViewById(R.id.progressWrapper)
        progressTitle = view.findViewById(R.id.progressTitle)
        progressBar = view.findViewById(R.id.progressBar)
        processingProgressBar = view.findViewById(R.id.processingProgressBar)

        tvToolbarTitle?.text = getString(R.string.edit_preview)

        closeButton.visibility = View.GONE

        waveformView.setZoomInMax(true)

        setOnlyShowPreview(true)

        nextButtonEdit.setOnClickListener {
            updatePreview()
        }
    }

    private fun updatePreview() {
        nextButtonEdit.isEnabled = false

        // TODO stop any currently playing media

        model.updatePreview(podcast = cast).observe(viewLifecycleOwner) { success ->
            if (success) {
                // dismiss
                EditPreviewDialog.DismissEvent.dismiss()
            } else {
                // TODO display error message?
                nextButtonEdit.isEnabled = true
            }
        }
    }

    override fun useCustomProgressCallback(): Boolean {
        return true
    }

    override fun onProcessingProgress(progress: Float) {
        val pb = processingProgressBar ?: return
        pb.progress = (progress * pb.max).toInt()
        if (progress >= 1) {
            progressWrapper?.visibility = View.GONE
            nextButtonEdit.isEnabled = true
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_waveform_condensed
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
                        lifecycleScope.launch { onDownloadComplete() }
                    }
                }
            } catch (e: Exception) {
                // TODO show error ?
                Log.d("sdvf", e.toString())
            }

            try {
                file.delete()
            } catch (t: Throwable) {
                // ignored
            }
        }
    }

    private fun onDownloadComplete() {
        progressTitle?.text = getString(R.string.processing_audio)
        progressBar?.visibility = View.GONE
        processingProgressBar?.visibility = View.VISIBLE

        loadFromFile()
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