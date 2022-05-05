package com.limor.app.util

import android.content.Context
import android.media.MediaPlayer
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
            onDone?.invoke()
            return;
        }

        val mediaPlayer = MediaPlayer()

        try {
            val afd = context.assets.openFd("sounds/${soundType.file}")
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

            mediaPlayer.setOnCompletionListener {
                onDone?.invoke()
            }

            mediaPlayer.prepare()
            mediaPlayer.start()

        } catch (throwable: Throwable) {
            onDone?.invoke()
            throwable.printStackTrace()
        }
    }
}