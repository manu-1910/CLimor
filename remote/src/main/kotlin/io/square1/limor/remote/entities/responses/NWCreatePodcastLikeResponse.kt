package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWCreatePodcastLikeResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPodcastDataLike? = NWPodcastDataLike()
)

@Serializable
data class NWPodcastDataLike(
    @Optional
    val like: NWPodcastLike? = NWPodcastLike()
)

@Serializable
data class NWPodcastLike (
    @Optional
    val podcast_id : Int = 0,
    @Optional
    val user_id : Int = 0
)