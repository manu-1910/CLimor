package com.limor.app.scenes.main_new.view.editpreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.audio.wav.WavHelper
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.scenes.utils.waveform.MarkerSet
import com.limor.app.scenes.utils.waveform.WaveformFragment
import com.limor.app.scenes.utils.waveform.soundfile.SoundFile
import com.limor.app.uimodels.CastUIModel
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.android.synthetic.main.fragment_waveform.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import java.io.*
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

    private var marker: MarkerSet? = null
    private var processingStartTime = 0L

    private val cast: CastUIModel by lazy {
        requireArguments().getParcelable(KEY_PODCAST)!!
    }

    private val audioFileName: String by lazy {
        getAudioFileName("wav")
    }

    private fun getAudioFileName(extension: String? = null): String {
        val castURL = cast.audio?.url ?: ""
        val downloadDirectory =
            File(requireContext().getExternalFilesDir(null)?.absolutePath, "/limorv2/download/")
        if (!downloadDirectory.exists()) {
            val isDirectoryCreated = downloadDirectory.mkdirs()
        }
        val fileName = castURL.substring(castURL.lastIndexOf("/") + 1)
        val targetName = downloadDirectory.absolutePath + "/" + fileName
        if (extension.isNullOrEmpty()) {
            return targetName
        } else {
            return "$targetName.$extension"
        }
    }

    override fun getFileName(): String {
        val fileName = getAudioFileName("wav")
        if (BuildConfig.DEBUG) {
            println("EditPreviewFragment will use $fileName")
        }
        return fileName
    }

    override fun populateMarkers() {
        val details = cast.patronDetails ?: return
        val startPixels = waveformView.adjustedMillisecsToPixels(details.startsAt ?: 0)
        val endPixels = waveformView.adjustedMillisecsToPixels(details.endsAt ?: 5000)
        marker =  addMarker(startPixels, endPixels, false, R.color.white)
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
        deleteAudio()
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
        val marker = marker ?: return
        val details = cast.patronDetails ?: return

        val startsAt = waveformView.adjustedPixelsToMillisecs(marker.startPos)
        val endsAt = waveformView.adjustedPixelsToMillisecs(marker.endPos)
        val durationMillis = endsAt - startsAt

        if (durationMillis < 30 * 1000) {
            LimorDialog(layoutInflater).apply {
                setTitle(R.string.preview_duration)
                setMessage(R.string.preview_duration_hint)
                addButton(R.string.ok, true)
            }.show()
            return
        }

        details.startsAt = startsAt
        details.endsAt = endsAt
        details.previewDuration = durationMillis

        nextButtonEdit.isEnabled = false

        // TODO stop any currently playing media

        model.updatePreview(podcast = cast).observe(viewLifecycleOwner) { success ->
            if (success) {
                deleteAudio()
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

        pb.post {
            pb.progress = (progress * pb.max).toInt()
            if (progress >= 1) {
                progressWrapper?.visibility = View.GONE
                nextButtonEdit.isEnabled = true

                if (BuildConfig.DEBUG) {
                    val end = System.currentTimeMillis()
                    println("EditPreviewFragment: Processing took ${end - processingStartTime}")
                }

            }
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_waveform_condensed
    }

    private fun downloadSamples() {
        val context = context ?: return
        val storageRef = Firebase.storage.reference
        val samplesFileName = "${cast.id}.samples"
        val samplesRef = storageRef.child("samples/$samplesFileName")
        val localFile = CommonsKt.getDownloadFile(context, samplesFileName)

        val start = System.currentTimeMillis()
        samplesRef.getFile(localFile).addOnSuccessListener {
            if (BuildConfig.DEBUG) {
                println("EditPreviewFragment: Downloaded samples file in ${System.currentTimeMillis() - start} ms. Has error -> ${it.error}")
            }
            if (setSamplesFile(localFile)) {
                fileName = getAudioFileName("")
                onDownloadComplete()
            } else {
                convertAudio()
            }

        }.addOnFailureListener {
            convertAudio()
        }

    }

    private fun setSamplesFile(localFile: File): Boolean {
        if (!localFile.exists()) {
            if (BuildConfig.DEBUG) {
                println("Samples file does not exist. Probably none uploaded to Firebase.")
            }
            return false
        }
        val start = System.currentTimeMillis()
        try {
            val fi = FileInputStream(localFile)
            val oi = ObjectInputStream(fi)

            val lean: SoundFile.Lean = oi.readObject() as SoundFile.Lean
            leanSoundFile = lean

            if (BuildConfig.DEBUG) {
                println("EditPreviewFragment: Created Lean file: $leanSoundFile in ${System.currentTimeMillis() - start} ms.")
            }

            oi.close()
            fi.close()

            return true

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            if (BuildConfig.DEBUG) {
                println("EditPreviewFragment: Could not create local file.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            if (BuildConfig.DEBUG) {
                println("EditPreviewFragment: IO error")
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            if (BuildConfig.DEBUG) {
                println("EditPreviewFragment: Could not find clas.")
            }
        }

        return false
    }

    private fun downloadCast() {
        val context = context ?: return
        val testURL = "https://limor-platform-development.s3-eu-west-1.amazonaws.com/podcast_audio_direct_upload/audioFile_163661872969286112_1636618729691.x-m4a"
        val testURL2 = "https://limor-platform-production.s3.eu-west-1.amazonaws.com/podcast_audio_direct_upload%2Fc8aef6e8-eb77-4d38-accf-ac2e3c173f39.m4a"
        val testURL3 = "https://limor-platform-development.s3.amazonaws.com/podcast_audio_direct_upload/_audioFile_4967_1653287354.083281.m4a"
        val castURL = cast.audio?.url ?: return
        val downloadURL = castURL
        val original = getAudioFileName()
        val file = File(original)

        try {
            file.delete()
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        try {
            File(getAudioFileName("wav"))
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        var start = System.currentTimeMillis()

        val fetchConfiguration = FetchConfiguration.Builder(requireContext())
            .setDownloadConcurrentLimit(5)
            .enableLogging(true)
            .build()

        val  fetch = Fetch.Impl.getInstance(fetchConfiguration);

        val request = Request(downloadURL, original).also {

        }


        val fetchListener: FetchListener = object : FetchListener {
            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {

            }

            override fun onCompleted(download: Download) {
                if (BuildConfig.DEBUG) {
                    println("EditPreviewFragment: Downloading took ${System.currentTimeMillis() - start} ms, downloaded to: $file")
                }
                fetch.remove(request.id)
                fetch.removeListener(this)

                onDownloadedCastAudio()
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {

            }

//            override fun onError(download: Download) {
//                val error: java.lang.Error = download.getError()
//            }

            override fun onProgress(
                download: Download,
                etaInMilliSeconds: Long,
                downloadedBytesPerSecond: Long
            ) {
                if (BuildConfig.DEBUG) {

                    println("EditPreviewFragment: Progress -> ${download.progress}, eta: ${etaInMilliSeconds}")
                }
            }

            override fun onPaused(download: Download) {}
            override fun onResumed(download: Download) {}
            override fun onStarted(
                download: Download,
                downloadBlocks: List<DownloadBlock>,
                totalBlocks: Int
            ) {
                if (BuildConfig.DEBUG) {

                    println("EditPreviewFragment: Started -> ${download.progress}")
                }
            }

            override fun onWaitingNetwork(download: Download) {

            }

            override fun onAdded(download: Download) {

            }

            override fun onCancelled(download: Download) {}
            override fun onRemoved(download: Download) {}
            override fun onDeleted(download: Download) {}
            override fun onDownloadBlockUpdated(
                download: Download,
                downloadBlock: DownloadBlock,
                totalBlocks: Int
            ) {

            }
        }

        fetch.addListener(fetchListener)

        fetch.enqueue(request,
            { updatedRequest: Request? ->
                println("EditPreviewFragment: Update request -> ${updatedRequest?.url} -> ${updatedRequest?.file}")
            }
        ) { error: Error? ->
            println("EditPreviewFragment: Error $error")
        }

        if (true) {
            return
        }

        val bufferSize = 1024 * 1024 * 32
        doAsync {
            try {
                URL(downloadURL).openStream().use { input ->
                    BufferedInputStream(input).use { bis ->
                        FileOutputStream(file).use { output ->

                            val data = ByteArray(bufferSize)
                            var count: Int
                            while (bis.read(data, 0, bufferSize).also { count = it } != -1) {
                                output.write(data, 0, count)
                            }
                            // input.copyTo(output)


                            if (BuildConfig.DEBUG) {
                                println("EditPreviewFragment: Downloading took ${System.currentTimeMillis() - start} ms, downloaded to: $file")
                            }
                            start = System.currentTimeMillis()
                            WavHelper.convertToWavFile(requireContext(), original, audioFileName)
                            if (BuildConfig.DEBUG) {
                                println("EditPreviewFragment: Converting took ${System.currentTimeMillis() - start} ms.")
                            }
                            lifecycleScope.launch { onDownloadComplete() }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                file.delete()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private fun onDownloadedCastAudio() {
        progressTitle?.text = context?.getString(R.string.converting_audio) ?: "Converting audio"

        downloadSamples()
    }

    private fun convertAudio() {
        lifecycleScope.launch {
            convertAudio(getAudioFileName())
        }
    }

    private fun deleteAudio() {
        val file = File(audioFileName)
        if (file.exists()) {
            try {
                file.delete()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private suspend fun convertAudio(original: String) {
        val context = context ?: return

        withContext(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            WavHelper.convertToWavFile(context, original, audioFileName)
            if (BuildConfig.DEBUG) {
                println("EditPreviewFragment: Converting took ${System.currentTimeMillis() - start} ms. Produced a file of ${File(audioFileName).length()} bytes.")
            }
            lifecycleScope.launch { onDownloadComplete() }
        }
    }

    private fun onDownloadComplete() {
        progressTitle?.text = getString(R.string.processing_audio)
        progressBar?.visibility = View.GONE
        processingProgressBar?.visibility = View.VISIBLE

        processingStartTime = System.currentTimeMillis()

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