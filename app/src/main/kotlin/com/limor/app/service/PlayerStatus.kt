package com.limor.app.service

sealed class PlayerStatus(open val podcastId: Int?) {
    data class Other(override val podcastId: Int? = null) : PlayerStatus(podcastId)
    data class Playing(override val podcastId: Int) : PlayerStatus(podcastId)
    data class Paused(override val podcastId: Int) : PlayerStatus(podcastId)
    data class Cancelled(override val podcastId: Int? = null) : PlayerStatus(podcastId)
    data class Ended(override val podcastId: Int) : PlayerStatus(podcastId)
    data class Error(override val podcastId: Int, val exception: Exception?) : PlayerStatus(podcastId)
}