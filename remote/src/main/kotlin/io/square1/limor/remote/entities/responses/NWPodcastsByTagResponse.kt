package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWPodcastsByTagResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPodcastsTagItemsArray = NWPodcastsTagItemsArray()
)

@Serializable
data class NWPodcastsTagItemsArray(
    @Optional
    val podcasts: ArrayList<NWPodcast> = ArrayList()
)