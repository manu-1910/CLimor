package com.limor.app.uimodels

data class UIGetPodcastResponse(
    var code: Int,
    var message: String,
    var data: UIGetPodcastItem
)

data class UIGetPodcastItem(
    var podcast: UIPodcast
)