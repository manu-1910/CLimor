package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<FeedResponseEntity>.asUIModel(): Single<UIFeedResponse> {
    return this.map { it.asUIModel() }
}


fun FeedResponseEntity.asUIModel(): UIFeedResponse {
    return UIFeedResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun FeedsItemsEntityArray.asUIModel(): UIFeedItemsArray {
    return UIFeedItemsArray(
        getAllUIFeedItems(feed_items)
    )
}


fun FeedsItemsEntity.asUIModel(): UIFeedItem {
    return UIFeedItem(
        id,
        podcast?.asUIModel(),
        user.asUIModel(),
        recasted,
        created_at
    )
}


fun getAllUIFeedItems(entityList: ArrayList<FeedsItemsEntity>?): ArrayList<UIFeedItem> {
    val uiList = ArrayList<UIFeedItem>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}
