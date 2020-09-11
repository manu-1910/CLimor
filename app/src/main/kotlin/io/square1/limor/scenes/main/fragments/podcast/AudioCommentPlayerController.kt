package io.square1.limor.scenes.main.fragments.podcast

import android.content.Context
import android.media.MediaPlayer
import android.os.Environment
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import io.square1.limor.scenes.utils.Commons
import io.square1.limor.uimodels.UIComment
import org.jetbrains.anko.doAsync
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class AudioCommentPlayerController(
    val comment: UIComment,
    private val seekBar: SeekBar,
    private val playButton: ImageButton,
    private val tvCurrentTime: TextView,
    private val tvTotalTime: TextView,
    private val context: Context
) {
    private lateinit var simpleRecorder : SimpleRecorder

    init {
        prepare()
    }

    private fun prepare() {
        downloadAudioComment()
    }

    fun onPlayClicked() {
        if(simpleRecorder.isPlaying)
            simpleRecorder.pausePlaying()
        else
            simpleRecorder.resumePlaying()
    }

    fun onSeekProgressChanged(progress: Int) {

    }

    private fun downloadAudioComment() {
        val url = comment.audio.url
        if(url != null) {
            val downloadDirectory = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/download/")
            if(!downloadDirectory.exists()){
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
                        simpleRecorder.startPlaying(destinationFile.absolutePath,
                            MediaPlayer.OnCompletionListener {
                                Toast.makeText(
                                    context,
                                    "Comment audio completed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                    }
                }
            }
        }
    }

      // not working, should be deleted in the future
//    private fun downloadAudioCommentAWS() {
//        // Note: this is not the audio file name, it's a directory.
//        val downloadDirectory = File(Environment.getExternalStorageDirectory()?.absolutePath + "/limorv2/download/")
//        if(!downloadDirectory.exists()){
//            downloadDirectory.mkdir()
//        }
//
//        //Upload audio file to AWS
//        Commons.getInstance().downloadAudio(
//            context,
//            comment.audio.url,
//            downloadDirectory.absolutePath,
//            object : Commons.AudioDownloadCallback {
//                override fun onSuccess(downloadedFile: File) {
//                    simpleRecorder = SimpleRecorder(downloadedFile.absolutePath)
//                    simpleRecorder.startPlaying(downloadedFile.absolutePath,
//                        MediaPlayer.OnCompletionListener {
//                            Toast.makeText(
//                                context,
//                                "Comment audio completed",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        })
//                }
//
//                override fun onError(error: String?) {
//                    Timber.d("Audio upload to AWS error: $error")
//                }
//            })
//    }

}