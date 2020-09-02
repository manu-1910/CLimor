package io.square1.limor.uimodels

data class UIPodcastsByTagResponse(
    var code: Int,
    var message: String,
    var data: UIPodcastsTagItemsArray = UIPodcastsTagItemsArray()
)

data class UIPodcastsTagItemsArray(
    var podcasts: ArrayList<UIPodcast> = ArrayList()
)