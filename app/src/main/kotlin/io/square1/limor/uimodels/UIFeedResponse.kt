package io.square1.limor.uimodels

data class UIFeedResponse(
    var code: Int,
    var message: String,
    var data: UIFeedItemsArray = UIFeedItemsArray()
)

data class UIFeedItemsArray(
    var feed_items: ArrayList<UIFeedItems> = ArrayList()
)

data class UIFeedItems(
    var id: String,
    var podcast: UIPodcast?,
    var user: UIUser,
    var recasted: Boolean,
    var created_at: Int
)