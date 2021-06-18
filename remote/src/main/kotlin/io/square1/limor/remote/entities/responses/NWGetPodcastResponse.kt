package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWGetPodcastResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWPodcastData = NWPodcastData()
)

@Serializable
data class NWPodcastData(

    val podcast: NWPodcast = NWPodcast()
)