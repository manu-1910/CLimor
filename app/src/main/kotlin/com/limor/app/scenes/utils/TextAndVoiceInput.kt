package com.limor.app.scenes.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.media.AudioFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import com.limor.app.App
import com.limor.app.R
import com.limor.app.audio.wav.waverecorder.WaveRecorder
import com.limor.app.extensions.*
import com.limor.app.scenes.utils.voicePlayer.LimorMediaPlayer
import com.limor.app.util.hasRecordPermissions
import kotlinx.android.synthetic.main.item_input_with_audio.view.*
import kotlin.math.min
import java.io.File
import java.util.*
import kotlin.math.sqrt

sealed class InputStatus

object None : InputStatus()

object StartRecord : InputStatus()

object FinishRecord : InputStatus()

object ListenRecord : InputStatus()

object PauseRecord : InputStatus()

object MissingPermissions: InputStatus()

class SendData(val text: String, val filePath: String?, val duration: Int) : InputStatus()

class TextAndVoiceInput @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var status: InputStatus = None
        set(value) {
            statusListener?.invoke(value)
        }
    private var filePath: String? = null
    private var durationMillis: Int = 0
    private var mediaDuration: Long = 0

    lateinit var editText: EditText

    private val mediaPlayer: LimorMediaPlayer by lazy {
        val mp = LimorMediaPlayer()
        mp.setOnCompletionListener { onReplayComplete() }
        mp.setOnErrorListener { _, _, _ -> true }
        mp
    }

    private val seekUpdater: Runnable
    private val seekHandler: Handler by lazy {
        Handler()
    }

    private var mRecorder: WaveRecorder? = null

    private val timer = object : CountDownTimer(180000, 1000) {
        @SuppressLint("SetTextI18n")
        override fun onTick(millisUntilFinished: Long) {
            // this is safe to call even though the timer supports Long milliseconds, the max Int
            // value would be enough to store 24 days worth of milliseconds and also the timer is
            // actually set to 3 minutes max
            val millis = millisUntilFinished.toInt()

            durationMillis = 180000 - millis

            val actualSeconds = millis / 1000
            val minutes = actualSeconds.div(60)
            val seconds = actualSeconds % 60
            tvTime.text = "$minutes:${getSeconds(seconds)}"
        }

        override fun onFinish() {
            // Stop the recording, because the time limit was reached
            setRecording(false)

            // Just to be sure there aren't any rounding/calculation issues we set the label
            // to 0:00
            tvTime.text = "0:00"
        }

    }

    private var isRecording = false

    private fun getSeconds(seconds: Int) = if (seconds < 10) "0$seconds" else "$seconds"

    private var statusListener: ((InputStatus) -> Unit)? = null

    init {
        initView()
        readAttributes(attrs)

        // 60 fps = 16.667 ms per frame, but 15 fps is good enough for this
        val updateInterval = (16.667 * 4).toLong()

        seekUpdater = object : Runnable {
            override fun run() {
                mediaPlayer.let {
                    if (it.isPlaying && it.currentPosition > 0 && mediaDuration > 0) {
                        val positionRatio =
                            min(1f, it.currentPosition.toFloat() / mediaDuration.toFloat())

                        positionIndicator.x = (positionRatio - 1) * positionIndicator.width
                    }
                }
                seekHandler.postDelayed(this, updateInterval)
            }
        }
    }

    private fun updateSendButtonState() {
        val isActivated = comment_text.text.toString().isNotEmpty() || !filePath.isNullOrEmpty()
        btnPodcastSendComment.isActivated = isActivated
    }

    private fun initView() {
        inflate(context, R.layout.item_input_with_audio, this)
        editText = comment_text

        comment_text.addTextChangedListener {
            updateSendButtonState()
        }

        btnPodcastSendComment.setOnClickListener {
            if (btnPodcastSendComment.isActivated) {
                status = SendData(comment_text.text.toString(), filePath, durationMillis)
                showRecordingControls(false)
                comment_text.text = null
                filePath = null
                updateSendButtonState()
            }
        }

        btnPodcastStartVoiceComment.setOnClickListener {
            if (hasRecordPermissions(context)) {
                showRecordingControls(true)
                setRecording(true)
            } else {
                status = MissingPermissions
            }
        }

        btnDeleteVoice.setOnClickListener {
            showRecordingControls(false)
            setRecording(false)
            status = None

            deleteLastFile()
        }

        btnPodcastStartStopVoice.setOnClickListener {
            setRecording(!it.isActivated)
        }

        btnStartPlay.setOnClickListener {
            replay()
        }

        visualizer.ampNormalizer = { sqrt(it.toFloat()).toInt() }

        layoutPlayer.clipToOutline = true
        layoutPlayer.clipChildren = true
        layoutPlayer.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(0, 0, view!!.width, view.height, 24.px.toFloat())
            }

        }
    }

    private fun deleteLastFile() {
        pausePlayer()
        filePath?.let {
            val file = File(it)
            file.delete()
        }
        filePath = null
        updateSendButtonState()
    }

    private fun onReplayComplete() {
        btnStartPlay.isActivated = false
        resetPositionIndicator()
    }

    private fun resetPositionIndicator() {
        positionIndicator.x = -positionIndicator.width.toFloat()
        positionIndicator.makeInVisible()
    }

    private fun replay() {
        if (filePath.isNullOrEmpty()) {
            return
        }

        if (mediaPlayer.lastDataSource != filePath) {
            // this is a new data source and the easiest thing to do is to dispose of the current
            // media player and create a new one
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            playWithFile()
            return
        }

        // Pause the play if already playing
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnStartPlay.isActivated = false
            return
        }

        // Resume the play if was previously paused
        if (mediaPlayer.currentPosition > 0) {
            mediaPlayer.start()
            btnStartPlay.isActivated = true

            // when an audio completed playing the current position remains at the end of the
            // audio and because of the completion listener we hide the playing indicator, so
            // it has to be shown again
            positionIndicator.makeVisible()
            return
        }

        playWithFile()
    }

    private fun playWithFile() {
        // If not pausing or resuming the play then this is the first time the audio is played
        btnStartPlay.isActivated = true

        if (mediaDuration == 0L) {
            val uri: Uri = Uri.parse(filePath)
            uri.path?.let {
                val f = File(it)
                if (f.exists()) {
                    val mmr = MediaMetadataRetriever()
                    try {
                        mmr.setDataSource(context, uri)
                        val durationStr =
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        mediaDuration = durationStr!!.toLong()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        positionIndicator.x = -positionIndicator.width.toFloat()
        positionIndicator.makeVisible()

        mediaPlayer.setDataSource(filePath)

        try {
            mediaPlayer.prepare()
            mediaPlayer.start()
            seekHandler.post(seekUpdater)

        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun showRecordingControls(show: Boolean) {
        btnStartPlay.makeGone()
        btnPodcastStartVoiceComment.visibility = if (show) View.GONE else View.VISIBLE
        layoutRecordingControls.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setRecording(shouldRecord: Boolean) {

        if (!hasRecordPermissions(context)) {
            status = MissingPermissions
            return
        }

        btnPodcastStartStopVoice.isActivated = shouldRecord

        if (shouldRecord) {
            startRecording()
        } else {
            stopRecording()
        }

    }

    private fun pausePlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnStartPlay.isActivated = false
        }
    }

    private fun resetRecorderWithNewFile() {
        // delete any previously recorded file
        if (!filePath.isNullOrEmpty()) {
            deleteLastFile()
        }

        pausePlayer()

        resetPositionIndicator()

        val recordingDirectory =
            File(context?.getExternalFilesDir(null)?.absolutePath + "/limorv2/")
        if (!recordingDirectory.exists()) {
            recordingDirectory.mkdir()
        }
        val fileName = getCurrentTimeString() + CommonsKt.audioFileFormat
        val file = File(recordingDirectory, fileName)
        filePath = file.path

        val recorder = WaveRecorder(file.path)
        mRecorder = recorder

        recorder.waveConfig.apply {
            sampleRate = 44100
            channels = AudioFormat.CHANNEL_IN_STEREO
            audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        }

        visualizer.clear()
        recorder.onAmplitudeListener = {
            if (isRecording && it != 0) {
                visualizer.post {
                    visualizer.addAmp(it, mRecorder!!.tickDuration)
                }
            }
        }

    }

    private fun pauseAnyPlayingMedia() {
        App.instance.playerBinder.pauseCurrentTrack()
        pausePlayer()
    }

    private fun startRecording() {
        pauseAnyPlayingMedia()
        horizontalLine.makeInVisible()

        isRecording = true

        resetRecorderWithNewFile()
        mRecorder?.startRecording()

        status = StartRecord

        mediaDuration = 0

        timer.start()
        visualizer.makeVisible()

    }

    private fun stopRecording() {
        if (isRecording) {
            mRecorder?.stopRecording()
        }

        status = FinishRecord

        timer.cancel()

        btnStartPlay.makeVisible()

        visualizer.makeInVisible()
        horizontalLine.makeVisible()

        updateSendButtonState()
    }

    private fun getCurrentTimeString(): String {
        val currentDate = Date()
        return currentDate.time.toString()
    }

    fun initListenerStatus(data: (InputStatus) -> Unit) {
        statusListener = data
        statusListener?.invoke(status)
    }

    private fun readAttributes(attrs: AttributeSet?) {
//        val typedArray =
//            context.obtainStyledAttributes(attrs, R.styleable.CustomBtn, 0, 0)
//        state = typedArray.getInt(R.styleable.CustomBtn_cbtn_state, 0)
//        val text = typedArray.getString(R.styleable.CustomBtn_cbtn_name) ?: ""
//        changeState(state)
//        setTextBtn(text)
    }

}