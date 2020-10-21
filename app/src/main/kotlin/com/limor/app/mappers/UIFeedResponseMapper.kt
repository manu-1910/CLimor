package com.limor.app.mappers

import com.limor.app.uimodels.UIFeedItem
import com.limor.app.uimodels.UIFeedItemsArray
import com.limor.app.uimodels.UIFeedResponse
import entities.response.FeedResponseEntity
import entities.response.FeedsItemsEntity
import entities.response.FeedsItemsEntityArray
import io.reactivex.Single


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
