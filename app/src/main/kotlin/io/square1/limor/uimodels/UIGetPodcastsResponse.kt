package io.square1.limor.uimodels

data class UIGetPodcastsResponse(
    var code: Int,
    var message: String,
    var data: UIGetPodcastsItemsArray = UIGetPodcastsItemsArray()
)

data class UIGetPodcastsItemsArray(
    var podcasts: ArrayList<UIPodcast> = ArrayList()
)