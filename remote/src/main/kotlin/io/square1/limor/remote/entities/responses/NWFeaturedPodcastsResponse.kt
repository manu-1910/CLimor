package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable


@Serializable
data class NWFeaturedPodcastsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWFeaturedPodcastsArray = NWFeaturedPodcastsArray()
)

@Serializable
data class NWFeaturedPodcastsArray(

    val podcasts: ArrayList<NWPodcast> = ArrayList()
)