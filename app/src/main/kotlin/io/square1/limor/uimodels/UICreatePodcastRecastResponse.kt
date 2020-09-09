package io.square1.limor.uimodels

data class UICreatePodcastRecastResponse (
    var code: Int,
    var message: String,
    var data: UICreatePodcastRecastData?
)

data class UICreatePodcastRecastData(
    var recast: UIPodcastRecast?
)

data class UIPodcastRecast (
    var podcast_id : Int,
    var user_id : Int
)