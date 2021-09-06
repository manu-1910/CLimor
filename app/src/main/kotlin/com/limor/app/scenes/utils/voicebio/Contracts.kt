package com.limor.app.scenes.utils.voicebio

import android.content.Context
import kotlinx.coroutines.CoroutineScope

interface VoiceBioContract {
    interface ViewModel {
        fun getNextAudioFilePath(): String
        fun getAudioURL(): String?
        fun setAudioInfo(path: String? = null, durationSeconds: Double? = null)
        fun addAmp(amp: Int, tickDuration: Int)
        fun resetVisualization()
        fun ensurePermissions(): Boolean
        fun getScope(): CoroutineScope
        fun getContext(): Context
    }

    interface Presenter {
        fun startRecording()
        fun stopRecording()

        fun playStopRecord()
        fun deleteRecord()

        fun setAudioURL(url: String?)
    }
}