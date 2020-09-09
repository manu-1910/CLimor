package io.square1.limor.scenes.main.fragments.podcast

import android.media.MediaPlayer
import android.media.MediaRecorder
import io.square1.limor.scenes.utils.Commons
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*


class SimpleRecorder(private val folderPath: String) {

    private var recorder = MediaRecorder()
    private var player = MediaPlayer()
    private var lastFileName = ""
    var maxAmplitude = recorder.maxAmplitude
    var isPlaying = false
    var isReleased = true
    var isPaused = !isPlaying && player.currentPosition > 1

    fun startRecording() {
        val directory = File(folderPath)
        require(!(!directory.exists() || !directory.isDirectory)) { "[AMRAudioRecorder] audioFileDirectory is a not valid directory!" }

        lastFileName = directory.absolutePath + "/" + Date().time + Commons.audioFileFormat
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(lastFileName)

            try {
                prepare()
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }

            start()
        }
    }

    fun stopRecording(): String {
        recorder.apply {
            stop()
            release()
        }
        return lastFileName
    }


    fun startPlaying(fileName : String, completionListener: MediaPlayer.OnCompletionListener) {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
                this@SimpleRecorder.isReleased = false
                this@SimpleRecorder.isPlaying = true
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }
        }
        player.setOnCompletionListener{
            isPlaying = false
            completionListener.onCompletion(it)
        }
    }

    fun resumePlaying() {
        isPlaying = true
        player.start()
    }

    fun pausePlaying() {
        isPlaying = false
        player.pause()
    }

    fun stopPlaying() {
        player.release()
        isReleased = true
        isPlaying = false
    }

}