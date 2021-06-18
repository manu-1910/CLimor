package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWCreatePodcastLikeResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWPodcastCreateLikeData? = NWPodcastCreateLikeData()
)

@Serializable
data class NWPodcastCreateLikeData(

    val like: NWPodcastLike? = NWPodcastLike()
)

@Serializable
data class NWPodcastLike (

    val podcast_id : Int = 0,

    val user_id : Int = 0
)