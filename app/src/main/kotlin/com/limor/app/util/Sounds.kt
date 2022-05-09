package com.limor.app.util

import android.content.Context
import android.media.MediaPlayer
import com.limor.app.BuildConfig
import com.limor.app.scenes.auth_new.util.PrefsHandler

enum class SoundType(val file: String) {
    HEART("heart.wav"),
    MESSAGE("message.wav"),
    NOTIFICATION("notification.wav"),
    PAYMENT("payment.wav"),
    RECAST("recast.wav"),
    COMMENT("comment.wav"),
}

object Sounds {
    fun playSound(context: Context, soundType: SoundType, onDone: (() -> Unit)? = null) {
        if (!PrefsHandler.areSoundsEnabled(context)) {
            if (BuildConfig.DEBUG) {
                println("Will not play sound as sounds are disabled.")
            }
            onDone?.invoke()
            return
        }

        val mediaPlayer = MediaPlayer()

        try {
            val afd = context.assets.openFd("sounds/${soundType.file}")
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

            mediaPlayer.prepare()
            mediaPlayer.start()

            if (BuildConfig.DEBUG) {
                println("Played sound, notifying done.")
            }
            onDone?.invoke()

        } catch (throwable: Throwable) {
            onDone?.invoke()
            throwable.printStackTrace()
        }
    }
}