package com.limor.app.service

sealed class PlayerStatus {
    object Init : PlayerStatus()
    object Other : PlayerStatus()
    object Playing : PlayerStatus()
    object Paused : PlayerStatus()
    object Cancelled : PlayerStatus()
    object Ended : PlayerStatus()
    object Buffering : PlayerStatus()
    data class Error(val exception: Exception?) : PlayerStatus()
}