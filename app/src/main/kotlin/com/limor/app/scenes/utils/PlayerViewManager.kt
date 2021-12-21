package com.limor.app.scenes.utils

import com.limor.app.service.AudioService
import com.limor.app.uimodels.TagUIModel

interface PlayerViewManager {
    fun isPlayerVisible(): Boolean
    fun showPlayer(args: PlayerArgs, onTransitioned: (() -> Unit)? = null)
    fun hidePlayer()
    fun navigateToHashTag(hashtag: TagUIModel)
    fun playPreview(audio: AudioService.AudioTrack, startPosition: Int, endPosition: Int)
    fun stopPreview(reset: Boolean)
    fun isPlayingComment(audioTrack: AudioService.AudioTrack): Boolean
    fun isPlaying(audioTrack: AudioService.AudioTrack): Boolean

    data class PlayerArgs(
        val playerType: PlayerType,
        val castId: Int,
        val maximizedFromMiniPlayer: Boolean = false,
        val restarted: Boolean = false
    )

    enum class PlayerType {
        EXTENDED, SMALL, TINY
    }
}

fun PlayerViewManager.showExtendedPlayer(castId: Int) {
    showPlayer(
        PlayerViewManager.PlayerArgs(
            PlayerViewManager.PlayerType.EXTENDED,
            castId
        )
    )
}