package io.square1.limor.scenes.main.fragments.podcast

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import io.square1.limor.scenes.utils.Commons
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*


class SimpleRecorder(private val folderPath: String) {

    private var recorder = MediaRecorder()
    private var player = MediaPlayer()
    private var lastFileName = ""
    var isPlayerPlaying = false
    var isPlayerReleased = true
    var isPlayerPaused = !isPlayerPlaying && !isPlayerReleased

    var isRecorderRecording = false

    fun startRecording() {
        Timber.d("Inside startRecording")
        val directory = File(folderPath)
        require(!(!directory.exists() || !directory.isDirectory)) { "[AMRAudioRecorder] audioFileDirectory is a not valid directory!" }

        lastFileName = directory.absolutePath + "/" + Date().time + Commons.audioFileFormat
        recorder = MediaRecorder()
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder.setOutputFile(lastFileName)
        try {
            recorder.prepare()
        } catch (e: IOException) {
            Timber.d("Media recorder prepare failed")
        }

        Timber.d("Inside startRecording just before start")
        recorder.start()
        isRecorderRecording = true
    }

    fun stopRecording(): Pair<String, Int> {
        var duration = 0
        if(isRecorderRecording) {
            recorder.stop()
            recorder.release()

            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(lastFileName)
            val durationStr =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration = durationStr.toInt() / 1000

        }
        return Pair(lastFileName, duration)
    }


    fun startPlaying(fileName: String, completionListener: MediaPlayer.OnCompletionListener) {
        Timber.d("Inside startPlaying")
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
                this@SimpleRecorder.isPlayerReleased = false
                this@SimpleRecorder.isPlayerPlaying = true
            } catch (e: IOException) {
                Timber.e("prepare() failed")
            }
        }
        player.setOnCompletionListener {
            isPlayerPlaying = false
            completionListener.onCompletion(it)
        }
    }

    fun getCurrentPosition() : Int {
        return player.currentPosition
    }

    fun resumePlaying() {
        isPlayerPlaying = true
        player.start()
    }

    fun pausePlaying() {
        isPlayerPlaying = false
        player.pause()
    }

    fun stopPlaying() {
        try {
            player.stop()
        } catch (e:Exception){}
        try {
            player.release()
        } catch (e:Exception){}
        isPlayerReleased = true
        isPlayerPlaying = false
    }

    fun getMaxAmplitude() : Int {
        return recorder.maxAmplitude
    }

    fun clear() {
        stopPlaying()
    }

    fun moveToPosition(progress: Int) {
        player.seekTo(progress)
    }

}