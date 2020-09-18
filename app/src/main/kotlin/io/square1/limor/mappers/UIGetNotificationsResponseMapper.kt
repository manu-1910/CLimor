package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*

fun Single<NotificationsResponseEntity>.asUIModel(): Single<UIGetNotificationsResponse> {
    return this.map { it.asUIModel() }
}


fun NotificationsResponseEntity.asUIModel(): UIGetNotificationsResponse {
    return UIGetNotificationsResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun NotificationsItemsEntityArray.asUIModel(): UIGetNotificationsItemsArray {
    return UIGetNotificationsItemsArray(
        getAllUINotificationItems(notification_items)
    )
}

fun NotificationItemsEntity.asUIModel(): UINotificationItem {
    return UINotificationItem(
        id,
        message,
        status,
        created_at,
        notification_type,
        resources.asUIModel()
    )
}

fun ResourcesEntity.asUIModel(): UIResources {
    return UIResources(
        participant_id,
        conversation_id,
        images.asUIModel(),
        ad?.asUIModel(),
        comment.asUIModel(),
        owner.asUIModel(),
        podcast?.asUIModel()
    )
}

fun AdEntity.asUIModel(): UIAdItem {

    return UIAdItem(
        id,
        user.asUIModel(),
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
        audio.asUIModel(),
        active,
        learn_more_url,
        links.asUIModel(),
        mentions.asUIModel(),
        tags.asUIModel(),
        sharing_url,
        learn_more_title
    )

}

fun GalleryEntity.asUIModel(): UIGallery {
    return UIGallery(
        id,
        images.asUIModel(),
        cover_image
    )
}

fun AdDataLinksEntity.asUIModel(): UIAdDataLinks {
    return UIAdDataLinks(
        getAllDataLinkEntities(content),
        getAllDataLinkEntities(text),
        getAllDataLinkEntities(caption)
    )
}

fun DataLinkEntity.asUIModel(): UIDataLink {
    return UIDataLink(
        id, link, start_index, end_index
    )
}

fun getAllDataLinkEntities(entityList: ArrayList<DataLinkEntity>): ArrayList<UIDataLink> {
    val uiList = ArrayList<UIDataLink>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun getAllUINotificationItems(entityList: ArrayList<NotificationItemsEntity>?): ArrayList<UINotificationItem> {
    val uiList = ArrayList<UINotificationItem>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun getAllGalleryItemEntities(entityList: ArrayList<GalleryEntity>?): ArrayList<UIGallery> {
    val uiList = ArrayList<UIGallery>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}