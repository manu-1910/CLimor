package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWFeaturedPodcastsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWFeaturedPodcastsArray = NWFeaturedPodcastsArray()
)

@Serializable
data class NWFeaturedPodcastsArray(
    @Optional
    val podcasts: ArrayList<NWPodcast> = ArrayList()
)