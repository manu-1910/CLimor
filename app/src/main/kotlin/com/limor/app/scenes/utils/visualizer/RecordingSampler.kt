package com.limor.app.scenes.utils.visualizer

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.limor.app.scenes.utils.VisualizerView
import java.util.*
import kotlin.math.abs

class RecordingSampler {
    private var mAudioRecord: AudioRecord? = null

    /**
     * getter isRecording
     *
     * @return true:recording, false:not recording
     */
    var isRecording = false
        private set
    private var mBufSize = 0
    private var mVolumeListener: CalculateVolumeListener? = null
    private var mSamplingInterval = 100
    private var mTimer: Timer? = null
    private val mVisualizerViews: MutableList<VisualizerView>? = ArrayList()

    /**
     * link to VisualizerView
     *
     * @param visualizerView [VisualizerView]
     */
    fun link(visualizerView: VisualizerView) {
        mVisualizerViews!!.add(visualizerView)
    }

    /**
     * setter of CalculateVolumeListener
     *
     * @param volumeListener CalculateVolumeListener
     */
    fun setVolumeListener(volumeListener: CalculateVolumeListener?) {
        mVolumeListener = volumeListener
    }

    /**
     * setter of samplingInterval
     *
     * @param samplingInterval interval volume sampling
     */
    fun setSamplingInterval(samplingInterval: Int) {
        mSamplingInterval = samplingInterval
    }

    private fun initAudioRecord() {
        val bufferSize = AudioRecord.getMinBufferSize(
            RECORDING_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        mAudioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            RECORDING_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        if (mAudioRecord!!.state == AudioRecord.STATE_INITIALIZED) {
            mBufSize = bufferSize
        }
    }

    /**
     * start AudioRecord.read
     */
    fun startRecording() {
        mTimer = Timer()
        mAudioRecord!!.startRecording()
        isRecording = true
        runRecording()
    }

    /**
     * stop AudioRecord.read
     */
    fun stopRecording() {
        isRecording = false
        mTimer!!.cancel()
        if (mVisualizerViews != null && !mVisualizerViews.isEmpty()) {
            for (i in mVisualizerViews.indices) {
                mVisualizerViews[i].receive(0)
            }
        }
    }

    private fun runRecording() {
        val buf = ByteArray(mBufSize)
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                // stop recording
                if (!isRecording) {
                    mAudioRecord!!.stop()
                    return
                }
                mAudioRecord!!.read(buf, 0, mBufSize)
                val decibel = calculateDecibel(buf)
                if (mVisualizerViews != null && !mVisualizerViews.isEmpty()) {
                    for (i in mVisualizerViews.indices) {
                        mVisualizerViews[i].receive(decibel)
                    }
                }

                // callback for return input value
                if (mVolumeListener != null) {
                    mVolumeListener!!.onCalculateVolume(decibel)
                }
            }
        }, 0, mSamplingInterval.toLong())
    }

    private fun calculateDecibel(buf: ByteArray): Int {
        var sum = 0
        for (i in 0 until mBufSize) {
            sum += abs(buf[i].toInt())
        }
        // avg 10-50
        return sum / mBufSize
    }

    /**
     * release member object
     */
    fun release() {
        stopRecording()
        mAudioRecord!!.release()
        mAudioRecord = null
        mTimer = null
    }

    interface CalculateVolumeListener {
        /**
         * calculate input volume
         *
         * @param volume mic-input volume
         */
        fun onCalculateVolume(volume: Int)
    }

    companion object {
        private const val RECORDING_SAMPLE_RATE = 44100
    }

    init {
        initAudioRecord()
    }
}