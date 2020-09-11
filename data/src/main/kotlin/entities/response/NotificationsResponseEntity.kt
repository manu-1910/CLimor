package entities.response

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
    val created_at: Long,
    val notification_type: String,
    val resources: ResourcesEntity
)

data class ResourcesEntity(
    val ad: AdEntity,
    val comment: CommentEntity,
    val owner: UserEntity
)

data class AdEntity(
    val id: Long,
    val user: UserEntity,
    val title: String,
    val address: String,
    val gallery: List<GalleryEntity>,
    val caption: String,
    val created_at: Long,
    val updated_at: Long,
    val latitude: Double,
    val longitude: Double,
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
    val sharing_url: String,
    val learn_more_title: String
)

data class GalleryEntity(
    val id: Int = 0,
    val images: ImagesEntity,
    val cover_image: Boolean
)

data class AdDataLinksEntity(
    val content: List<DataLinkEntity>,
    val text: List<DataLinkEntity>,
    val caption: List<DataLinkEntity>
)

data class DataLinkEntity(
    val id: Long,
    val link: String,
    val start_index: Int,
    val end_index: Int
)
