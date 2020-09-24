package entities.response

data class GetPodcastsResponseEntity(
    var code: Int,
    var message: String,
    var data: PodcastsItemsEntityArray
)

data class PodcastsItemsEntityArray(
    var podcasts: ArrayList<PodcastEntity>
)