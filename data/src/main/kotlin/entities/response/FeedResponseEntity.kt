package entities.response


data class FeedResponseEntity(
    var code: Int,
    var message: String,
    var data: FeedsItemsEntityArray
)

data class FeedsItemsEntityArray(
    var feed_items: ArrayList<FeedsItemsEntity>
)

data class FeedsItemsEntity(
    var id: String,
    var podcast: PodcastEntity?,
    var user: UserEntity,
    var recasted: Boolean,
    var created_at: Int
)

