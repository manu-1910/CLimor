package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWDeletePodcastLikeResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPodcastDeleteLikeData? = NWPodcastDeleteLikeData()
)

@Serializable
data class NWPodcastDeleteLikeData(
    @Optional
    val destroyed : Boolean? = false
)