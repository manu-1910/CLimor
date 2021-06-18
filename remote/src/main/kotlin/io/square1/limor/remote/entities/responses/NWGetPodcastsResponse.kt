package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWGetPodcastsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWPodcastsArray = NWPodcastsArray()
)

@Serializable
data class NWPodcastsArray(

    val podcasts: ArrayList<NWPodcast> = ArrayList()
)