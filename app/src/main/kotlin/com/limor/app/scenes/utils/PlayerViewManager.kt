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
        val castIds: List<Int>? = null,
        val maximizedFromMiniPlayer: Boolean = false,
        val restarted: Boolean = false,
        val playFrom: Int = 0
    )

    enum class PlayerType {
        EXTENDED, SMALL, TINY
    }
}

fun PlayerViewManager.showExtendedPlayer(castId: Int, castIds: List<Int>? = null) {
    showPlayer(
        PlayerViewManager.PlayerArgs(
            PlayerViewManager.PlayerType.EXTENDED,
            castId,
            castIds,
        )
    )
}