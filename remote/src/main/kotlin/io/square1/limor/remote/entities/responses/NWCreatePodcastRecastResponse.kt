package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWCreatePodcastRecastResponse (

    val code: Int = 0,

    val message: String = "",

    val data: NWPodcastCreateRecastData? = NWPodcastCreateRecastData()
)

@Serializable
data class NWPodcastCreateRecastData(

    val recast: NWPodcastRecast? = NWPodcastRecast()
)

@Serializable
data class NWPodcastRecast (

    val podcast_id : Int = 0,

    val user_id : Int = 0
)