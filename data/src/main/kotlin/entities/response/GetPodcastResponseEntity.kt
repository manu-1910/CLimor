package entities.response


data class GetPodcastResponseEntity(
    var code: Int,
    var message: String,
    var data: PodcastItemEntity
)

data class PodcastItemEntity(
    var podcast: PodcastEntity
)