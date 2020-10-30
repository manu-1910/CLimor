package com.limor.app.audio

abstract class AudioFacade {

    abstract fun startRecording()
    abstract fun stopRecording()
    abstract fun pauseRecording()
    abstract fun isPlaying() : Boolean
    abstract fun mergeTwoFiles(file1: String, file2: String, outputFile: String, skipFirst: Boolean = false, skipSecond: Boolean = false) : Boolean

}