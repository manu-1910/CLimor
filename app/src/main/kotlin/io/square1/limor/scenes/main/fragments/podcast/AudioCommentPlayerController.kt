package io.square1.limor.scenes.main.fragments.podcast

import android.media.MediaPlayer
import android.os.Environment
import android.os.Handler
import android.widget.ImageButton
import android.widget.SeekBar
import io.square1.limor.R
import io.square1.limor.uimodels.UIComment
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class AudioCommentPlayerController(
    val comment: UIComment,
    private val seekBar: SeekBar,
    private val playButton: ImageButton
) {
    private var simpleRecorder: SimpleRecorder? = null
    private val updater: Runnable
    private val handler: Handler = Handler()
    private var isPrepared: Boolean = false

    init {
        prepareAudio()
        comment.audio.duration?.let { seekBar.max = it * 1000 }
        updater = object : Runnable {
            override fun run() {
                handler.postDelayed(this, 150)
                simpleRecorder?.let {
                    if(it.isPlayerPlaying) {
                        val currentPosition = it.getCurrentPosition()
                        seekBar.progress = currentPosition
                    }
                }
            }
        }
    }

    private fun prepareAudio() {
        downloadAudioComment()
    }

    fun onPlayClicked() {
        if(isPrepared) {
            simpleRecorder?.let {
                if (it.isPlayerPlaying) {
                    playButton.setImageResource(R.drawable.play)
                    it.pausePlaying()
                    handler.removeCallbacks(updater)
                } else {
                    playButton.setImageResource(R.drawable.pause)
                    it.resumePlaying()
                    handler.post(updater)
                }
            }
        }
    }

    fun onSeekProgressChanged(progress: Int) {
        simpleRecorder?.moveToPosition(progress)
    }

    private fun downloadAudioComment() {
        val url = comment.audio.url
        if (url != null) {
            val downloadDirectory =
                File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/download/")
            if (!downloadDirectory.exists()) {
                downloadDirectory.mkdir()
            }
            val fileName = url.substring(url.lastIndexOf("/") + 1)
            val finalPath: String = downloadDirectory.absolutePath + "/" + fileName
            val destinationFile = File(finalPath)
            doAsync {
                URL(url).openStream().use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                        simpleRecorder = SimpleRecorder(destinationFile.absolutePath)
                        simpleRecorder?.startPlaying(destinationFile.absolutePath,
                            MediaPlayer.OnCompletionListener {
                                onCompletionListener()
                            })
                        if (seekBar.progress > 0)
                            simpleRecorder?.moveToPosition(seekBar.progress)
                        playButton.setImageResource(R.drawable.pause)
                        handler.post(updater)
                        isPrepared = true
                    }
                }
            }
        }
    }

    private fun onCompletionListener() {
        seekBar.progress = 0
        playButton.setImageResource(R.drawable.play)
        handler.removeCallbacks(updater)
    }

    fun destroy() {
        simpleRecorder?.let {
            it.stopPlaying()
            handler.removeCallbacks(updater)
            playButton.setImageResource(R.drawable.play)
        }
    }

}