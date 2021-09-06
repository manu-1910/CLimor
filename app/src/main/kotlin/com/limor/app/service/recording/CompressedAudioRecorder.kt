package com.limor.app.service.recording

import android.content.Context
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Handler
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicBoolean

interface RecorderCallback {
    fun onStartRecord()
    fun onPauseRecord()
    fun onResumeRecord()
    fun onRecordProgress(millis: Long, amp: Int)
    fun onStopRecord()
    fun onError(throwable: Throwable?)
}

interface Recorder {
    var callback: RecorderCallback?

    fun isRecording(): Boolean
    fun isPaused(): Boolean
    fun startRecording(path: String, context: Context)
    fun resumeRecording()
    fun pauseRecording()
    fun stopRecording(context: Context)
}

object CompressedAudioRecorder : Recorder {

    private const val RECORDING_SAMPLE_RATE = 44100
    private const val RECORD_ENCODING_BITRATE_128000 = 128000
    private const val RECORDING_VISUALIZATION_INTERVAL_MILLIS = 16L

    override var callback: RecorderCallback? = null

    private var recorder: MediaRecorder? = null
    private val isRecording = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val handler = Handler()
    private var updateTime: Long = 0
    private var durationMills: Long = 0

    override fun isRecording(): Boolean {
        return isRecording.get()
    }

    override fun isPaused(): Boolean {
        return isPaused.get()
    }

    private fun ensureRecorder() {
        if (null != recorder) {
            // release previous resources
            //
            releaseRecorderResources()
        }

        this.recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(2)
            setAudioSamplingRate(RECORDING_SAMPLE_RATE)
            setAudioEncodingBitRate(RECORD_ENCODING_BITRATE_128000)
            setMaxDuration(-1)
        }
    }

    override fun startRecording(path: String, context: Context) {
        if (isRecording.get()) {
            stopRecording(context)
        }

        RecordService.start(context, RecordService.ACTION_START_RECORDING_SERVICE)

        ensureRecorder()
        val recorder = recorder ?: return
        val recordFile = File(path)

        recorder.setOutputFile(recordFile.absolutePath)
        try {
            recorder.prepare()
            recorder.start()
            updateTime = System.currentTimeMillis()
            isRecording.set(true)
            callback?.onStartRecord()
            scheduleRecordingTimeUpdate()
            isPaused.set(false)
        } catch (e: IOException) {
            e.printStackTrace()
            callback?.onError(e)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            callback?.onError(e)
        }
    }

    private fun scheduleRecordingTimeUpdate() {
        handler.postDelayed({
            if (isRecording()) {
                val curTime = System.currentTimeMillis()
                durationMills += curTime - updateTime
                updateTime = curTime
                val amp = try { recorder?.maxAmplitude ?: 0 } catch (t: Throwable) { 0 }
                callback?.onRecordProgress(durationMills, amp)
                scheduleRecordingTimeUpdate()
            }
        }, RECORDING_VISUALIZATION_INTERVAL_MILLIS)
    }

    override fun resumeRecording() {
        if (isPaused.get()) {
            try {
                recorder!!.resume()
                updateTime = System.currentTimeMillis()
                scheduleRecordingTimeUpdate()
                callback?.onResumeRecord()
                isPaused.set(false)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                callback?.onError(e)
            }
        }
    }

    override fun pauseRecording() {
        if (isRecording.get()) {
            if (!isPaused.get()) {
                try {
                    recorder!!.pause()
                    durationMills += System.currentTimeMillis() - updateTime
                    pauseRecordingTimer()
                    callback?.onPauseRecord()
                    isPaused.set(true)
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    callback?.onError(e)
                }
            }
        }
    }

    private fun releaseRecorderResources() {
        val recorder = recorder ?: return
        try {
            recorder.stop()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        recorder.release()
    }

    override fun stopRecording(context: Context) {

        if (isRecording.get()) {
            RecordService.start(context, RecordService.ACTION_STOP_RECORDING_SERVICE)
            stopRecordingTimer()
            releaseRecorderResources()
            callback?.onStopRecord()
            durationMills = 0
            isRecording.set(false)
            isPaused.set(false)
            recorder = null
        } else {
            Timber.e("Recording has already stopped or hasn't started")
        }
    }

    private fun stopRecordingTimer() {
        handler.removeCallbacksAndMessages(null)
        updateTime = 0
    }

    private fun pauseRecordingTimer() {
        handler.removeCallbacksAndMessages(null)
        updateTime = 0
    }

}