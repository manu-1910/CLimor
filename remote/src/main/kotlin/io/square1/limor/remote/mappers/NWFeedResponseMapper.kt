package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWFeedResponse>.asDataEntity(): Single<FeedResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWFeedResponse.asDataEntity(): FeedResponseEntity {
    return FeedResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWFeedItemsArray.asDataEntity(): FeedsItemsEntityArray {
    return FeedsItemsEntityArray(
        getAllFeedItemssEntities(feed_items)
    )
}

fun NWFeedItems.asDataEntity(): FeedsItemsEntity{
    return FeedsItemsEntity(
        id,
        podcast?.asDataEntity(),
        user.asDataEntity(),
        recasted,
        created_at

        // TODO Jose add ad again
        //ad
    )
}



fun getAllFeedItemssEntities(nwList: ArrayList<NWFeedItems>?): ArrayList<FeedsItemsEntity> {
    val entityList = ArrayList<FeedsItemsEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}

