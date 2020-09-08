package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWCreatePodcastRecastResponse (
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPodcastCreateRecastData? = NWPodcastCreateRecastData()
)

@Serializable
data class NWPodcastCreateRecastData(
    @Optional
    val recast: NWPodcastRecast? = NWPodcastRecast()
)

@Serializable
data class NWPodcastRecast (
    @Optional
    val podcast_id : Int = 0,
    @Optional
    val user_id : Int = 0
)