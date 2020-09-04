package entities.response

data class PodcastsByTagResponseEntity(
    var code: Int,
    var message: String,
    var data: PodcastsTagItemsEntityArray
)

data class PodcastsTagItemsEntityArray(
    var podcasts: ArrayList<PodcastEntity>
)