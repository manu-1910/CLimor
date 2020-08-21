package io.square1.limor.uimodels

data class UIDeletePodcastLikeResponse (
    var code: Int,
    var message: String,
    var data: UIDeletePodcastLikeData?
)

data class UIDeletePodcastLikeData(
    var destroyed: Boolean?
)