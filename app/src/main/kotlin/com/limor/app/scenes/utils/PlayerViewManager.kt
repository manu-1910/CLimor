package com.limor.app.scenes.utils

import com.limor.app.uimodels.CastUIModel

interface PlayerViewManager {
    fun isPlayerVisible(): Boolean
    fun showPlayer(args: PlayerArgs)
    fun hidePlayer()

    data class PlayerArgs(
        val playerType: PlayerType,
        val cast: CastUIModel
    )

    enum class PlayerType {
        EXTENDED, SMALL, TINY
    }
}

fun PlayerViewManager.showExtendedPlayer(cast: CastUIModel) {
    showPlayer(
        PlayerViewManager.PlayerArgs(
            PlayerViewManager.PlayerType.EXTENDED,
            cast
        )
    )
}