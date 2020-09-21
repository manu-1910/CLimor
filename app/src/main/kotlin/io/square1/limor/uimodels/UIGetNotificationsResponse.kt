package io.square1.limor.uimodels


data class UIGetNotificationsResponse(
    var code: Int,
    var message: String,
    var data: UIGetNotificationsItemsArray
)

data class UIGetNotificationsItemsArray(
    var notificationItems: ArrayList<UINotificationItem>
)

data class UINotificationItem(
    val id: Int,
    val message: String,
    val status: String,
    val createdAt: String,
    val notificationType: String,
    val resources: UIResources
)

data class UIResources(
    val participantId: Int,
    val conversationId: Int,
    val images: UIImages,
    val ad: UIAdItem?,
    val comment: UIComment,
    val owner: UIUser,
    val podcast: UIPodcast?
)

data class UIAdItem(
    val id: Long,
    val user: UIUser,
    val title: String,
    val address: String?,
    val gallery: List<UIGallery>,
    val caption: String,
    val createdAt: Long,
    val updatedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val liked: Boolean,
    val viewed: Boolean,
    val reported: Boolean,
    val listened: Boolean,
    val numberOfListens: Long,
    val numberOfLikes: Long,
    val numberOfComments: Long,
    val numberOfViews: Long,
    val audio: UIAudio,
    val active: Boolean,
    val learnMoreUrl: String,
    val links: UIAdDataLinks,
    val mentions: UIMentions,
    val tags: UITags,
    val sharingUrl: String?,
    val learnMoreTitle: String?
)

data class UIGallery(
    val id: Int = 0,
    val images: UIImages,
    val coverImage: Boolean
)

data class UIAdDataLinks(
    val content: List<UIDataLink>,
    val text: List<UIDataLink>,
    val caption: List<UIDataLink>
)

data class UIDataLink(
    val id: Long,
    val link: String,
    val startIndex: Int,
    val endIndex: Int
)