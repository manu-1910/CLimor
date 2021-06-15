package io.square1.limor.remote.entities.responses

import kotlinx.serialization.*

@Serializable
data class NWNotificationsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWNotificationsItemArray = NWNotificationsItemArray()
)

@Serializable
data class NWNotificationsItemArray(

    val notifications: ArrayList<NWNotificationItem> = ArrayList()
)

@Serializable
data class NWNotificationItem(

    val id: Int = 0,

    val message: String = "",

    val status: String = "",

    val created_at: String = "",

    val notification_type: String = "",

    val resources: NWResources = NWResources()
)

@Serializable
data class NWResources(


    val participant_id: Int = 0,

    val conversation_id: Int = 0,

    val images: NWImages = NWImages(),



    val ad: NWAd? = NWAd(),

    val comment: NWComment = NWComment(),

    val owner: NWUser = NWUser(),

    val podcast: NWPodcast? = NWPodcast()
)

@Serializable
data class NWAd(

    val id: Long = 0L,

    val user: NWUser = NWUser(),

    val title: String = "",

    val address: String? = "",

    val gallery: ArrayList<NWGallery> = ArrayList(),

    val caption: String = "",

    val created_at: Long = 0L,

    val updated_at: Long = 0L,

    val latitude: Double? = 0.0,

    val longitude: Double? = 0.0,

    val liked: Boolean = false,

    val viewed: Boolean = false,

    val reported: Boolean = false,

    val listened: Boolean = false,

    val number_of_listens: Long = 0L,

    val number_of_likes: Long = 0L,

    val number_of_comments: Long = 0L,

    val number_of_views: Long = 0L,

    val audio: NWAudio = NWAudio(),

    val active: Boolean = false,

    val learn_more_url: String = "",

    val links: NWAdDataLinks = NWAdDataLinks(),

    val mentions: NWMentions = NWMentions(),

    val tags: NWTags = NWTags(),

    val sharing_url: String? = "",

    val learn_more_title: String? = ""
)

@Serializable
data class NWGallery(

    val id: Int = 0,

    val images: NWImages = NWImages(),

    val cover_image: Boolean = false
)

@Serializable
data class NWAdDataLinks(

    val content: ArrayList<NWDataLink> = ArrayList(),

    val text: ArrayList<NWDataLink> = ArrayList(),

    val caption: ArrayList<NWDataLink> = ArrayList()
)

@Serializable
data class NWDataLink(

    val id: Long = 0L,

    val link: String = "",

    val start_index: Int = 0,

    val end_index: Int = 0
)


//{
//    "id": 7383,
//    "created_at": "2019-02-08T12:45:50.726Z",
//    "notification_type": "conversation_participant",
//    "message": "You have been added to the group \"I'm Groot \"",
//    "status": "sent",
//    "resources": {
//    "participant_id": 130,
//    "conversation_id": 211,
//    "images": {
//        "small_url": "https://limor-platform-development.s3.amazonaws.com/conversations/conversations/images/missing/missing.jpg",
//        "medium_url": "https://limor-platform-development.s3.amazonaws.com/conversations/conversations/images/missing/missing.jpg",
//        "large_url": "https://limor-platform-development.s3.amazonaws.com/conversations/conversations/images/missing/missing.jpg",
//        "original_url": "https://limor-platform-development.s3.amazonaws.com/conversations/conversations/images/missing/missing.jpg"
//    }
//}

