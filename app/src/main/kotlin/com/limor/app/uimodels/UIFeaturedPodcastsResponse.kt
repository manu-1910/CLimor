package com.limor.app.uimodels


data class UIFeaturedPodcastsResponse(
    val code: Int,
    val message: String,
    val data: FeaturedPodcastsArray = FeaturedPodcastsArray()

)

data class FeaturedPodcastsArray(
    val podcasts: ArrayList<UIPodcast> = ArrayList()
)