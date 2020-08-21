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
    var podcast: PodcastEntity?, // caution, this is declared in another class and I don't know if I can reuse it
    var user: UserEntity,
    var recasted: Boolean,
    var created_at: Int

    // TODO Jose add ad again
    //var ad: String // caution, in this example response the value returned is always null, so I don't know which value type it really is
)

