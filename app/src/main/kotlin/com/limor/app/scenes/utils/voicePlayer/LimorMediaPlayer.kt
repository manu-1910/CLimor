package com.limor.app.scenes.utils.voicePlayer

import android.media.MediaPlayer

class LimorMediaPlayer : MediaPlayer() {

    var lastDataSource: String? = null

    override fun setDataSource(path: String?) {
        super.setDataSource(path)
        lastDataSource = path
    }
}