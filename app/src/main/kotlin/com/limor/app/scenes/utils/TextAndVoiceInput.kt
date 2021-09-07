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
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import com.limor.app.App
import com.limor.app.R
import com.limor.app.audio.wav.waverecorder.WaveRecorder
import com.limor.app.extensions.*
import com.limor.app.scenes.utils.voicePlayer.LimorMediaPlayer
import com.limor.app.scenes.utils.voicebio.VoiceBioPresenter
import com.limor.app.service.recording.CompressedAudioRecorder
import com.limor.app.service.recording.RecorderCallback
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
) : FrameLayout(context, attrs, defStyleAttr), RecorderCallback {

    var progress: Int = 0
        set(value) {
            field = value

            // preparing for having a progress bar
            // progressUpload.visibility = if (value == 0 || value == 100) View.INVISIBLE else View.VISIBLE
            // progressUpload.progress = value

            if (value == 100) {
                reset()
            }
        }

    private var status: InputStatus = None
        set(value) {
            statusListener?.invoke(value)
        }
    private var filePath: String? = null
    private var durationMillis: Int = 0
    private var mediaDuration: Long = 0
    private var currentDuration = 0L

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
                        updatePosition(it.currentPosition)
                    }
                }
                seekHandler.postDelayed(this, updateInterval)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePosition(millis: Int) {
        val actualSeconds = millis / 1000
        val minutes = actualSeconds.div(60)
        val seconds = actualSeconds % 60
        tvTime.text = "$minutes:${getSeconds(seconds)}"
    }

    private fun updateSendButtonState() {
        val isActivated = !CompressedAudioRecorder.isRecording() &&
                (comment_text.text.toString().isNotEmpty() || !filePath.isNullOrEmpty())
        btnPodcastSendComment.isActivated = isActivated
    }

    private fun enableChildren(enable: Boolean) {
        this.allChildren {
            if (it is ViewGroup) {
                return@allChildren
            }
            it.isEnabled = enable
            it.alpha = if (enable) 1.0f else 0.2f
        }
    }

    private fun sendComment() {
        enableChildren(false)
        status = SendData(comment_text.text.toString(), filePath, durationMillis)
        pausePlayer()
    }

    fun reset() {
        showRecordingControls(false)
        comment_text.text = null
        filePath = null
        updateSendButtonState()
        enableChildren(true)
    }

    private fun initView() {
        CompressedAudioRecorder.callback = this

        inflate(context, R.layout.item_input_with_audio, this)
        editText = comment_text

        comment_text.addTextChangedListener {
            updateSendButtonState()
        }

        btnPodcastSendComment.setOnClickListener {
            if (btnPodcastSendComment.isActivated) {
                sendComment()
            }
        }

        btnPodcastStartVoiceComment.setOnClickListener {
            if (hasRecordPermissions(context)) {
                showRecordingControls(true)
                setRecording(true)
                updateSendButtonState()
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

        mediaDuration = currentDuration

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

    private fun resetRecorderWithNewFile(): String {
        // delete any previously recorded file
        if (!filePath.isNullOrEmpty()) {
            deleteLastFile()
        }

        pausePlayer()
        resetPositionIndicator()
        visualizer.clear()

        return CompressedAudioRecorder.getNextAudioFilePath(context).also {
            filePath = it
        }
    }

    private fun pauseAnyPlayingMedia() {
        App.instance.playerBinder.pauseCurrentTrack()
        pausePlayer()
    }

    private fun onBeforeRecording() {
        pauseAnyPlayingMedia()
        horizontalLine.makeInVisible()

        status = StartRecord
        mediaDuration = 0
        currentDuration = 0
        visualizer.makeVisible()
    }

    private fun startRecording() {
        onBeforeRecording()

        CompressedAudioRecorder.apply {
            val path = resetRecorderWithNewFile()
            startRecording(path, context)
        }
    }

    private fun stopRecording() {
        CompressedAudioRecorder.stopRecording(context)

        status = FinishRecord

        btnStartPlay.makeVisible()

        visualizer.makeInVisible()
        horizontalLine.makeVisible()

        updateSendButtonState()
        resetPositionLabel()
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

    override fun onStartRecord() {

    }

    override fun onPauseRecord() {

    }

    override fun onResumeRecord() {

    }

    override fun onRecordProgress(millis: Long, amp: Int) {
        // Long -> Int is safe because the tick time is very small, around 16 ms

        val delta = millis - currentDuration
        currentDuration = millis
        durationMillis = currentDuration.toInt()
        visualizer.addAmp(amp, delta.toInt())

        val remainingMillis = maxOf(0,  maxVoiceCommentDurationMillis - millis)
        if (remainingMillis == 0L) {
            setRecording(false)
            resetPositionLabel()
        }

        updatePosition(remainingMillis.toInt())
    }

    override fun onStopRecord() {

    }

    override fun onError(throwable: Throwable?) {

    }

    @SuppressLint("SetTextI18n")
    private fun resetPositionLabel() {
        tvTime.text = "0:00"
    }

    override fun onDetachedFromWindow() {
        if (CompressedAudioRecorder.callback == this) {
            CompressedAudioRecorder.callback = null
        }
        CompressedAudioRecorder.stopRecording(context)
        pausePlayer()
        super.onDetachedFromWindow()
    }

    companion object {
        const val maxVoiceCommentDurationMillis = 3 * 60 * 1000
    }
}