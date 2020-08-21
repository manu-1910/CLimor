package io.square1.limor.uimodels

data class UICreatePodcastLikeResponse (
    var code: Int,
    var message: String,
    var data: UICreatePodcastLikeData?
)

data class UICreatePodcastLikeData(
    var like: UIPodcastLike?
)

data class UIPodcastLike (
    var podcast_id : Int,
    var user_id : Int
)