package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWGetPodcastResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPodcastData = NWPodcastData()
)

@Serializable
data class NWPodcastData(
    @Optional
    val podcast: NWPodcast = NWPodcast()
)