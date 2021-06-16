package com.limor.app.util

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.lang.Exception

class CrashReportingTree: Timber.Tree() {

    companion object {
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }

    private val crashlytics = Firebase.crashlytics

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.ERROR) {
            val throwable = t ?: Exception(message)

            crashlytics.setCustomKeys {
                tag?.let { key(CRASHLYTICS_KEY_TAG, it) }
                key(CRASHLYTICS_KEY_MESSAGE, message)
            }

            crashlytics.recordException(throwable)
        }
    }
}
