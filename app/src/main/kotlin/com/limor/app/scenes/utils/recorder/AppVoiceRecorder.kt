package com.limor.app.scenes.utils.recorder

import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File


class AppVoiceRecorder {

    private val mMediaRecorder = MediaRecorder()
    private lateinit var mFile: File

    fun startRecord(newFile: File) {
        try {
            mFile = newFile

            prepareMediaRecorder()
            mMediaRecorder.start()
        } catch (e: Exception) {
            Log.e("!!!error startRecord", e.toString())
        }
    }

    fun stopRecord() {
        try {
            mMediaRecorder.stop()
        } catch (e: Exception) {
            Log.e("!!!error stopRecord", e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun pauseRecord() {
        try {
            mMediaRecorder.pause()
        } catch (e: Exception) {
            Log.e("!!!error pauseRecord", e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun resumeRecord() {
        try {
            mMediaRecorder.resume()
        } catch (e: Exception) {
            Log.e("!!!error resumeRecord", e.toString())
        }
    }

    fun releaseRecorder() {
        try {
            mMediaRecorder.release()
        } catch (e: Exception) {
            Log.e("!!!error releaseRecord", e.toString())
        }
    }

    private fun prepareMediaRecorder() {
        mMediaRecorder.apply {
            reset()
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            setOutputFile(mFile.absolutePath)
            prepare()
        }
    }


}