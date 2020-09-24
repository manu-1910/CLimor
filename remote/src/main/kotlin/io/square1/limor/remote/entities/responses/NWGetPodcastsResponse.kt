package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWGetPodcastsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPodcastsArray = NWPodcastsArray()
)

@Serializable
data class NWPodcastsArray(
    @Optional
    val podcasts: ArrayList<NWPodcast> = ArrayList()
)