package com.limor.app.uimodels


data class UIPopularPodcastsResponse(
    val code: Int,
    val message: String,
    val data: PopularPodcastsArray = PopularPodcastsArray()

)

data class PopularPodcastsArray(
    val podcasts: ArrayList<UIPodcast> = ArrayList()
)