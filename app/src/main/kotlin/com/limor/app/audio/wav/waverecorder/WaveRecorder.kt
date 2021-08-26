package com.limor.app.audio.wav.waverecorder

/*
 * MIT License
 *
 * Copyright (c) 2019 squti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import kotlinx.coroutines.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The WaveRecorder class used to record Waveform audio file using AudioRecord class to get the audio stream in PCM encoding
 * and then convert it to WAVE file (WAV due to its filename extension) by adding appropriate headers. This class uses
 * Kotlin Coroutine with IO dispatcher to writing input data on storage asynchronously.
 * @property filePath the path of the file to be saved.
 */
class WaveRecorder(private var filePath: String) {
    /**
     * Configuration for recording audio file.
     */
    var waveConfig: WaveConfig = WaveConfig()

    /**
     * Register a callback to be invoked in every recorded chunk of audio data
     * to get max amplitude of that chunk.
     */
    var onAmplitudeListener: ((Int) -> Unit)? = null

    /**
     * Register a callback to be invoked in recording state changes
     */
    var onStateChangeListener: ((RecorderState) -> Unit)? = null

    /**
     * Activates Noise Suppressor during recording if the device implements noise
     * suppression.
     */
    var noiseSuppressorActive: Boolean = false

    /**
     * The ID of the audio session this WaveRecorder belongs to.
     * The default value is -1 which means no audio session exist.
     */
    var audioSessionId: Int = -1
        private set

    private var isRecording = AtomicBoolean() // false by default
    private var isPaused = AtomicBoolean() // false by default
    private lateinit var audioRecorder: AudioRecord
    private var noiseSuppressor: NoiseSuppressor? = null

    val bufferSize: Int
        get() = AudioRecord.getMinBufferSize(
            waveConfig.sampleRate,
            waveConfig.channels,
            waveConfig.audioEncoding
        )

    private val channelCount: Int
        get() = if (waveConfig.channels == AudioFormat.CHANNEL_IN_MONO) 1 else 2

    val byteRate: Long
        get() = (bitPerSample * waveConfig.sampleRate * channelCount / 8).toLong()

    val tickDuration: Int
        get() = (bufferSize.toDouble() * 1000 / byteRate).toInt()

    private val bitPerSample: Int
        get() = when (waveConfig.audioEncoding) {
            AudioFormat.ENCODING_PCM_8BIT -> 8
            AudioFormat.ENCODING_PCM_16BIT -> 16
            else -> 16
        }

    /**
     * Starts audio recording asynchronously and writes recorded data chunks on storage.
     */
    fun startRecording() {

        if (!isAudioRecorderInitialized()) {
            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                waveConfig.sampleRate,
                waveConfig.channels,
                waveConfig.audioEncoding,
                bufferSize
            )

            audioSessionId = audioRecorder.audioSessionId

            isRecording.set(true)

            audioRecorder.startRecording()

            if (noiseSuppressorActive) {
                noiseSuppressor = NoiseSuppressor.create(audioRecorder.audioSessionId)
            }

            onStateChangeListener?.let {
                it(RecorderState.RECORDING)
            }
            GlobalScope.launch(Dispatchers.IO) {
                writeAudioDataToStorage()
            }
        }
    }

    // This runs on Dispatchers.IO only
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun writeAudioDataToStorage() {
        val bufferSize = AudioRecord.getMinBufferSize(
            waveConfig.sampleRate,
            waveConfig.channels,
            waveConfig.audioEncoding
        )
        val data = ByteArray(bufferSize)
        val outputStream = File(filePath).outputStream()
        while (isRecording.get()) {
            val readResult = audioRecorder.read(data, 0, bufferSize)

            // operationStatus is either 0 (SUCCESS) or > 0 indicating the number of bytes that
            // were read
            if (readResult >= AudioRecord.SUCCESS && !isPaused.get()) {
                outputStream.write(data, 0, if (readResult > 0) readResult else bufferSize)

                onAmplitudeListener?.let { reportAmplitude ->
                    withContext(Dispatchers.Default) {
                        reportAmplitude(data.calculateAmplitude())
                    }
                }
            }
        }

        outputStream.close()
        noiseSuppressor?.release()
    }

    private fun calculateAmplitudeMax(data: ByteArray): Int {
        val shortData = ShortArray(data.size / 2)
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            .get(shortData)

        return shortData.maxOrNull()?.toInt() ?: 0
    }

    /**
     * Stops audio recorder and release resources then writes recorded file headers.
     */
    fun stopRecording() {

        // always reset the flag to avoid any sync issues
        isRecording.set(false)

        if (isAudioRecorderInitialized()) {
            audioRecorder.stop()
            audioRecorder.release()
            audioSessionId = -1
            WaveHeaderWriter(filePath, waveConfig).writeHeader()
            onStateChangeListener?.let {
                it(RecorderState.STOP)
            }
        }

    }

    fun writeHeaders(filePath: String) {
        WaveHeaderWriter(filePath, waveConfig).writeHeader()
    }


    private fun isAudioRecorderInitialized(): Boolean =
        this::audioRecorder.isInitialized && audioRecorder.state == AudioRecord.STATE_INITIALIZED

    fun pauseRecording() {
        isPaused.set(true)
        onStateChangeListener?.let {
            it(RecorderState.PAUSE)
        }
    }

    fun resumeRecording() {
        isPaused.set(false)
        onStateChangeListener?.let {
            it(RecorderState.RECORDING)
        }
    }

}

fun ByteArray.calculateAmplitude(): Int {
    return ShortArray(size / 2).let {
        ByteBuffer.wrap(this)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(it)
        it.maxOrNull()?.toInt() ?: 0
    }
}