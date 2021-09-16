package com.limor.app.scenes.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import com.limor.app.App
import com.limor.app.MediaPlayerHandler
import com.limor.app.R
import com.limor.app.audio.wav.waverecorder.WaveRecorder
import com.limor.app.extensions.*
import com.limor.app.scenes.main_new.fragments.mentions.UserMentionPopup
import com.limor.app.scenes.utils.voicePlayer.LimorMediaPlayer
import com.limor.app.scenes.utils.voicebio.VoiceBioPresenter
import com.limor.app.service.recording.CompressedAudioRecorder
import com.limor.app.service.recording.RecorderCallback
import com.limor.app.uimodels.CommentUIModel
import com.limor.app.util.hasRecordPermissions
import kotlinx.android.synthetic.main.item_input_with_audio.view.*
import org.jetbrains.anko.appcompat.v7.tintedImageView
import org.jetbrains.anko.textColor
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

class SendData(val text: String, val filePath: String?, val duration: Int, val existingComment: CommentUIModel?) : InputStatus()

class TextAndVoiceInput @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), RecorderCallback, MediaPlayerHandler {

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

    private var editingComment: CommentUIModel? = null

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
        App.instance.registerMediaPlayerHandler(this)

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
            it.alpha = if (enable) 1.0f else disabledAlpha
        }
    }

    private fun sendComment() {
        enableChildren(false)
        status = SendData(
            text = comment_text.text.toString(),
            filePath = filePath,
            duration = durationMillis,
            existingComment = editingComment
        )
        pausePlayer()
    }

    fun reset() {
        showRecordingControls(false)
        comment_text.text = null
        filePath = null
        updateSendButtonState()
        enableChildren(true)
        editingComment = null

        toggleRecordButton(true)
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
        updatePosition(currentDuration.toInt())
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
            App.instance.playerBinder.pauseCurrentTrack()
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
            App.instance.playerBinder.pauseCurrentTrack()
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

        resetFlashAnimation()
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
        currentDuration = 0

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

        btnStartPlay.makeGone()
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

        enableMediaPlay()

        visualizer.makeInVisible()
        horizontalLine.makeVisible()

        updateSendButtonState()
    }

    private fun enableMediaPlay() {
        btnStartPlay.makeVisible()
        updatePosition(currentDuration.toInt())
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
        updatePosition(remainingMillis.toInt())

        if (remainingMillis == 0L) {
            setRecording(false)
        } else if (remainingMillis < warningThresholdMillis) {
            animateFlashPosition()
        }
    }

    private fun animateFlashPosition() {
        if (tvTime.animation != null) {
            return
        }
        val flashAnimation = AlphaAnimation(1.0f, 0.2f).apply {
            duration = 750
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        tvTime.apply {
            clearAnimation()
            startAnimation(flashAnimation)
            textColor = Color.RED
        }
    }

    private fun resetFlashAnimation() {
        tvTime.apply {
            clearAnimation()
            textColor = Color.BLACK
        }
    }

    override fun onStopRecord() {

    }

    override fun onError(throwable: Throwable?) {

    }

    @SuppressLint("SetTextI18n")
    private fun resetPositionLabel() {
        tvTime.text = "0:00"
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (visibility != View.VISIBLE) {
            setRecording(false)
        }
        super.onVisibilityChanged(changedView, visibility)
    }

    override fun onDetachedFromWindow() {
        if (CompressedAudioRecorder.callback == this) {
            CompressedAudioRecorder.callback = null
        }
        CompressedAudioRecorder.stopRecording(context)
        App.instance.unregisterMediaPlayerHandler(this)
        pausePlayer()
        super.onDetachedFromWindow()
    }

    fun toggleRecordButton(enabled: Boolean) {
        btnPodcastStartVoiceComment.isEnabled = enabled
        btnPodcastStartVoiceComment.alpha = if (enabled) 1.0f else disabledAlpha
    }

    fun edit(comment: CommentUIModel) {
        this.editingComment = comment
        setRecording(false)
        deleteLastFile()
        status = None

        toggleRecordButton(false)

        editText.apply {
            val newText= comment.content ?: ""
            setText(newText)
            highlight(UserMentionPopup.userMentionPattern, R.color.waveFormColor)
            setSelection(newText.length)
            requestFocus()
            showKeyboard()
        }
    }

    companion object {
        // we use 11 seconds because when the current position is say at 10.5 seconds the label
        // would display 10:00, so we need to start flashing the position indicator for any
        // position below 11 seconds
        const val warningThresholdMillis = 11 * 1000 // 11 seconds
        const val maxVoiceCommentDurationMillis = 3 * 60 * 1000 // 3 minutes
        const val disabledAlpha = 0.2f
    }

    override fun interruptPlaying() {
        pausePlayer()
        setRecording(false)
    }
}