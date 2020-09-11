package io.square1.limor.remote.entities.responses

import kotlinx.serialization.*

@Serializable
data class NWNotificationsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWNotificationsItemArray = NWNotificationsItemArray()
)

@Serializable
data class NWNotificationsItemArray(
    @Optional
    val notifications: ArrayList<NWNotificationItem> = ArrayList()
)

@Serializable
data class NWNotificationItem(
    @Optional
    val id: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val status: String = "",
    @Optional
    val created_at: Long = 0,
    @Optional
    val notification_type: String = "",
    @Optional
    val resources: NWResources = NWResources()
)

@Serializable
data class NWResources(
    @Optional
    val ad: NWAd = NWAd(),
    @Optional
    val comment: NWComment = NWComment(),
    @Optional
    val owner: NWUser = NWUser()
)

@Serializable
data class NWAd(
    @Optional
    val id: Long = 0,
    @Optional
    val user: NWUser = NWUser(),
    @Optional
    val title: String = "",
    @Optional
    val address: String = "",
    @Optional
    val gallery: ArrayList<NWGallery> = ArrayList(),
    @Optional
    val caption: String = "",
    @Optional
    val created_at: Long = 0,
    @Optional
    val updated_at: Long = 0,
    @Optional
    val latitude: Double = 0.0,
    @Optional
    val longitude: Double = 0.0,
    @Optional
    val liked: Boolean = false,
    @Optional
    val viewed: Boolean = false,
    @Optional
    val reported: Boolean = false,
    @Optional
    val listened: Boolean = false,
    @Optional
    val number_of_listens: Long = 0,
    @Optional
    val number_of_likes: Long = 0,
    @Optional
    val number_of_comments: Long = 0,
    @Optional
    val number_of_views: Long = 0,
    @Optional
    val audio: NWAudio = NWAudio(),
    @Optional
    val active: Boolean = false,
    @Optional
    val learn_more_url: String = "",
    @Optional
    val links: NWAdDataLinks = NWAdDataLinks(),
    @Optional
    val sharing_url: String = "",
    @Optional
    val learn_more_title: String = ""
)

@Serializable
data class NWGallery(
    @Optional
    val id: Int = 0,
    @Optional
    val images: NWImages = NWImages(),
    @Optional
    val cover_image: Boolean = false
)

@Serializable
data class NWAdDataLinks(
    @Optional
    val content: ArrayList<NWDataLink> = ArrayList(),
    @Optional
    val text: ArrayList<NWDataLink> = ArrayList(),
    @Optional
    val caption: ArrayList<NWDataLink> = ArrayList()
)

@Serializable
data class NWDataLink(
    @Optional
    val id: Long = 0,
    @Optional
    val link: String = "",
    @Optional
    val start_index: Int = 0,
    @Optional
    val end_index: Int = 0
)
