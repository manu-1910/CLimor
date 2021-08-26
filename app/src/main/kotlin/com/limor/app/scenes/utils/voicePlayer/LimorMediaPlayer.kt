package com.limor.app.scenes.utils.voicePlayer

import android.media.MediaPlayer

class LimorMediaPlayer : MediaPlayer() {

    var lastDataSource: String? = null

    override fun setDataSource(path: String?) {
        // much better for the audio to not play than to have the app crash on the user..
        try {
            super.setDataSource(path)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        lastDataSource = path
    }
}