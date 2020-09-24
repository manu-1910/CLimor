package io.square1.limor.uimodels


data class UIPopularPodcastsResponse(
    val code: Int,
    val message: String,
    val data: PopularPodcastsArray = PopularPodcastsArray()

)

data class PopularPodcastsArray(
    val podcasts: ArrayList<UIPodcast> = ArrayList()
)