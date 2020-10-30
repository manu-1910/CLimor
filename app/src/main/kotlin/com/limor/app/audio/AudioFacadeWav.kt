package com.limor.app.audio

import com.limor.app.audio.wav.WavHelper
import com.limor.app.audio.wav.waverecorder.WaveRecorder

class AudioFacadeWav : AudioFacade() {

    private val waveRecorder : WaveRecorder? = null

    override fun startRecording() {
        TODO("Not yet implemented")
    }

    override fun stopRecording() {
        TODO("Not yet implemented")
    }

    override fun pauseRecording() {
        TODO("Not yet implemented")
    }

    override fun isPlaying(): Boolean {
        TODO("Not yet implemented")
    }

    override fun mergeTwoFiles(
        file1: String,
        file2: String,
        outputFile: String,
        skipFirst: Boolean,
        skipSecond: Boolean
    ): Boolean {
        return WavHelper.combineWaveFile(file1, file2, outputFile, skipFirst, skipSecond)
    }

}