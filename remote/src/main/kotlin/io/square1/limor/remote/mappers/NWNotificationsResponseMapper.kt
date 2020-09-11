package io.square1.limor.remote.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*

fun Single<NWNotificationsResponse>.asDataEntity(): Single<NotificationsResponseEntity> {
    return this.map { it.asDataEntity() }
}


fun NWNotificationsResponse.asDataEntity(): NotificationsResponseEntity {
    return NotificationsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWNotificationsItemArray.asDataEntity(): NotificationsItemsEntityArray {
    return NotificationsItemsEntityArray(
        getAllNotificationItemEntities(notifications)
    )
}

fun NWNotificationItem.asDataEntity(): NotificationItemsEntity {
    return NotificationItemsEntity(
        id,
        message,
        status,
        created_at,
        notification_type,
        resources.asDataEntity()
    )
}

fun NWResources.asDataEntity(): ResourcesEntity {
    return ResourcesEntity(
        ad.asDataEntity(),
        comment.asDataEntity(),
        owner.asDataEntity()
    )
}

fun NWAd.asDataEntity(): AdEntity {

    return AdEntity(
        id,
        user.asDataEntity(),
        title,
        address,
        getAllGalleryItemEntities(gallery),
        caption,
        created_at,
        updated_at,
        latitude,
        longitude,
        liked,
        viewed,
        reported,
        listened,
        number_of_listens,
        number_of_likes,
        number_of_comments,
        number_of_views,
        audio.asDataEntity(),
        active,
        learn_more_url,
        links.asDataEntity(),
        sharing_url,
        learn_more_title
    )

}

fun NWGallery.asDataEntity(): GalleryEntity {
    return GalleryEntity(
        id,
        images.asDataEntity(),
        cover_image
    )
}

fun NWAdDataLinks.asDataEntity(): AdDataLinksEntity {
    return AdDataLinksEntity(
        getAllDataLinkEntities(content),
        getAllDataLinkEntities(text),
        getAllDataLinkEntities(caption)
    )
}

fun NWDataLink.asDataEntity(): DataLinkEntity {
    return DataLinkEntity(
        id, link, start_index, end_index
    )
}

fun getAllDataLinkEntities(nwList: ArrayList<NWDataLink>): ArrayList<DataLinkEntity> {
    val entityList = ArrayList<DataLinkEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}

fun getAllNotificationItemEntities(nwList: ArrayList<NWNotificationItem>?): ArrayList<NotificationItemsEntity> {
    val entityList = ArrayList<NotificationItemsEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}

fun getAllGalleryItemEntities(nwList: ArrayList<NWGallery>?): ArrayList<GalleryEntity> {
    val entityList = ArrayList<GalleryEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}