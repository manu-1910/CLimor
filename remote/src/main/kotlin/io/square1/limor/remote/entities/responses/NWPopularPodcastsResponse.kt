package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable


@Serializable
data class NWPopularPodcastsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWPopularPodcastsArray = NWPopularPodcastsArray()
)

@Serializable
data class NWPopularPodcastsArray(

    val podcasts: ArrayList<NWPodcast> = ArrayList()
)