package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWPopularPodcastsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPopularPodcastsArray = NWPopularPodcastsArray()
)

@Serializable
data class NWPopularPodcastsArray(
    @Optional
    val podcasts: ArrayList<NWPodcast> = ArrayList()
)