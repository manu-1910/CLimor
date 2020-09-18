package entities.response

import kotlinx.serialization.Optional

data class NotificationsResponseEntity(
    var code: Int,
    var message: String,
    var data: NotificationsItemsEntityArray
)

data class NotificationsItemsEntityArray(
    var notification_items: ArrayList<NotificationItemsEntity>
)

data class NotificationItemsEntity(
    val id: Int,
    val message: String,
    val status: String,
    val created_at: String,
    val notification_type: String,
    val resources: ResourcesEntity
)

data class ResourcesEntity(
    val participant_id: Int,
    val conversation_id: Int,
    val images: ImagesEntity,
    val ad: AdEntity?,
    val comment: CommentEntity,
    val owner: UserEntity,
    val podcast: PodcastEntity?
)

data class AdEntity(
    val id: Long,
    val user: UserEntity,
    val title: String,
    val address: String?,
    val gallery: ArrayList<GalleryEntity>,
    val caption: String,
    val created_at: Long,
    val updated_at: Long,
    val latitude: Double?,
    val longitude: Double?,
    val liked: Boolean,
    val viewed: Boolean,
    val reported: Boolean,
    val listened: Boolean,
    val number_of_listens: Long,
    val number_of_likes: Long,
    val number_of_comments: Long,
    val number_of_views: Long,
    val audio: AudioEntity,
    val active: Boolean,
    val learn_more_url: String,
    val links: AdDataLinksEntity,
    val mentions: MentionsEntity,
    val tags: TagsEntity,
    val sharing_url: String?,
    val learn_more_title: String?
)

data class GalleryEntity(
    val id: Int = 0,
    val images: ImagesEntity,
    val cover_image: Boolean
)

data class AdDataLinksEntity(
    val content: ArrayList<DataLinkEntity>,
    val text: ArrayList<DataLinkEntity>,
    val caption: ArrayList<DataLinkEntity>
)

data class DataLinkEntity(
    val id: Long,
    val link: String,
    val start_index: Int,
    val end_index: Int
)
