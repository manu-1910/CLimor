package com.limor.app.scenes.utils

import com.limor.app.uimodels.CastUIModel

interface PlayerViewManager {
    fun isPlayerVisible(): Boolean
    fun showPlayer(args: PlayerArgs)
    fun hidePlayer()

    data class PlayerArgs(
        val playerType: PlayerType,
        val castId: Int,
        val maximizedFromMiniPlayer: Boolean = false
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