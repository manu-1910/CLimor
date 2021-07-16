package com.limor.app.scenes.utils

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import com.limor.app.R
import com.limor.app.scenes.utils.recorder.AppVoiceRecorder
import com.limor.app.scenes.utils.visualizer.RecordingSampler
import kotlinx.android.synthetic.main.item_input_with_audio.view.*
import timber.log.Timber
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

sealed class InputStatus

object None : InputStatus()

object StartRecord : InputStatus()

object FinishRecord : InputStatus()

object ListenRecord : InputStatus()

object PauseRecord : InputStatus()

class SendData(val text: String, val filePath: String?, val duration: Long) : InputStatus()


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
    private var duration: Long = 0

    private val timer = object : CountDownTimer(180000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val actualSeconds = (180000 - millisUntilFinished)/1000
            Log.e("!!!", actualSeconds.toString())
            val minutes = actualSeconds.div(60)
            val seconds = actualSeconds % 60
            tvTime.text = "$minutes:${getSeconds(seconds)}"
            duration = actualSeconds
        }

        override fun onFinish() {

        }

    }

    private fun getSeconds(seconds: Long) = if(seconds < 10) "0$seconds"  else "$seconds"

    private var statusListener: ((InputStatus) -> Unit)? = null

    init {
        initView()
        readAttributes(attrs)
    }

    private fun initView() {
        inflate(context, R.layout.item_input_with_audio, this)
        btnPodcastSendComment.setOnClickListener {
            status = SendData(comment_text.text.toString(), filePath, duration)

        }
        btnPodcastStartVoiceComment.setOnClickListener {

            val appVoiceRecorder = AppVoiceRecorder()
            if(!it.isActivated) {
                val messageKey = getCurrentTimeString().replace(':', '_') + ".mp3"
                val mFile = File(context.filesDir, messageKey)
                appVoiceRecorder.startRecord(mFile)
                startRecord()
                btnPodcastStartVoiceComment.isActivated = true
                llVoice.visibility = VISIBLE
                btnDeleteVoice.visibility = VISIBLE
                status = StartRecord
                filePath = mFile.path
            } else {
                stopRecord()
                appVoiceRecorder.stopRecord()
                btnPodcastStartVoiceComment.isActivated = false
                status = FinishRecord
            }



        }

        btnDeleteVoice.setOnClickListener {
            stopRecord()
            btnPodcastStartVoiceComment.isActivated = false
            llVoice.visibility = View.GONE
            btnDeleteVoice.visibility = View.GONE
            status = None
        }

    }

    private fun stopRecord() {
        timer.cancel()
    }

    private fun startRecord() {
        timer.start()
    }

    private fun getCurrentTimeString(): String {
        val currentDate = Date()
        val timeFormat: DateFormat =
            SimpleDateFormat("hh:mm:ss dd.MM.yyyy", Locale.getDefault())
        return timeFormat.format(currentDate)
    }

    fun initListenerStatus(data: (InputStatus) -> Unit) {
        statusListener = data
        statusListener?.invoke(status)
    }

    fun requestFocus() {
        comment_text.requestFocus()
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