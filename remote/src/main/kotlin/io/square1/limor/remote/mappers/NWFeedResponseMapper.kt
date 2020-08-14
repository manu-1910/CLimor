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


fun NWFeedItemsArray.asDataEntity(): FeedsEntityArray {
    return FeedsEntityArray(
        getAllFeedItemssEntities(feed_items)
    )
}

fun NWFeedItems.asDataEntity(): FeedsEntity{
    return FeedsEntity(
        id,
        podcast?.asDataEntity(),
        user.asDataEntity(),
        recasted,
        created_at

        // TODO Jose add ad again
        //ad
    )
}



fun getAllFeedItemssEntities(nwList: ArrayList<NWFeedItems>?): ArrayList<FeedsEntity> {
    val entityList = ArrayList<FeedsEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}

