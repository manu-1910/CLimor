package io.square1.limor.uimodels

data class UIGetPodcastResponse(
    var code: Int,
    var message: String,
    var data: UIGetPodcastItem
)

data class UIGetPodcastItem(
    var podcast: UIPodcast
)