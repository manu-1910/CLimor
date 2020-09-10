package io.square1.limor.remote.entities.responses

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class NWNotificationsResponse(
    @Optional
    val code: Long,
    @Optional
    val message: String,
    @Optional
    val data: Data
)

@Serializable
data class Data(
    @Optional
    val notifications: List<Notification>
)

@Serializable
data class Notification(
    @Optional
    val id: Long,
    @Optional
    val message: String,
    @Optional
    val status: String,

    @Optional
    @SerialName("created_at")
    val createdAt: Long,

    @Optional
    @SerialName("notification_type")
    val notificationType: String,

    @Optional
    val resources: Resources
)

@Serializable
data class Resources(
    @Optional
    val ad: Ad,
    @Optional
    val comment: Comment,
    @Optional
    val owner: Owner
)

@Serializable
data class Ad(
    @Optional
    val id: Long,
    @Optional
    val user: User,
    @Optional
    val title: String,
    @Optional
    val address: String? = null,
    @Optional
    val gallery: List<Gallery>,
    @Optional
    val caption: String,

    @Optional
    @SerialName("created_at")
    val createdAt: Long,

    @Optional
    @SerialName("updated_at")
    val updatedAt: Long,

    @Optional
    val latitude: Double? = 0.0,
    @Optional
    val longitude: Double? = 0.0,
    @Optional
    val liked: Boolean,
    @Optional
    val viewed: Boolean,
    @Optional
    val reported: Boolean,
    @Optional
    val listened: Boolean,

    @Optional
    @SerialName("number_of_listens")
    val numberOfListens: Long,

    @Optional
    @SerialName("number_of_likes")
    val numberOfLikes: Long,

    @Optional
    @SerialName("number_of_comments")
    val numberOfComments: Long,

    @Optional
    @SerialName("number_of_views")
    val numberOfViews: Long,

    @Optional
    val audio: AdAudio,
    @Optional
    val active: Boolean,

    @Optional
    @SerialName("learn_more_url")
    val learnMoreURL: String,

//    @Optional
//    val tags: MentionsClass,
//    @Optional
//    val mentions: MentionsClass,
    @Optional
    val links: DataLinks,

    @Optional
    @SerialName("sharing_url")
    val sharingURL: String,

    @Optional
    @SerialName("learn_more_title")
    val learnMoreTitle: String
)

@Serializable
data class AdAudio(
    @Optional
    @SerialName("audio_url")
    val audioURL: String,

    @Optional
    @SerialName("total_length")
    val totalLength: Double,

    @Optional
    @SerialName("total_samples")
    val totalSamples: Long,

    @Optional
    val duration: Long,

    @Optional
    @SerialName("sample_rate")
    val sampleRate: Double,

    @Optional
    val timestamps: JsonArray,

    @Optional
    @SerialName("original_audio_url")
    val originalAudioURL: String
)

@Serializable
data class Gallery(
    @Optional
    val id: Long,
    @Optional
    val images: Images,

    @Optional
    @SerialName("cover_image")
    val coverImage: Boolean
)

@Serializable
data class Images(
    @Optional
    @SerialName("small_url")
    val smallURL: String,

    @Optional
    @SerialName("medium_url")
    val mediumURL: String,

    @Optional
    @SerialName("large_url")
    val largeURL: String,

    @Optional
    @SerialName("original_url")
    val originalURL: String
)

//class MentionsClass()

@Serializable
data class User(
    @Optional
    val id: Long,
    @Optional
    val username: String,

    @Optional
    @SerialName("first_name")
    val firstName: String,

    @Optional
    @SerialName("last_name")
    val lastName: String? = null,

    @Optional
    val images: Images,
    @Optional
    val blocked: Boolean,
    @Optional
    val followed: Boolean,

    @Optional
    @SerialName("blocked_by")
    val blockedBy: Boolean,

    @Optional
    @SerialName("followed_by")
    val followedBy: Boolean,

    @Optional
    val email: String,

    @Optional
    @SerialName("following_count")
    val followingCount: Long,

    @Optional
    @SerialName("followers_count")
    val followersCount: Long,

    @Optional
    val description: String? = null,
    @Optional
    val website: String? = null,
    @Optional
    val gender: String? = null,

    @Optional
    @SerialName("date_of_birth")
    val dateOfBirth: Long,

    @Optional
    @SerialName("phone_number")
    val phoneNumber: String? = null,

    @Optional
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean,

    @Optional
    val active: Boolean,
    @Optional
    val suspended: Boolean,
    @Optional
    val verified: Boolean,

    @Optional
    @SerialName("autoplay_enabled")
    val autoplayEnabled: Boolean,

    @Optional
    @SerialName("sharing_url")
    val sharingURL: String,

    @Optional
    val links: DataLinks,

    @Optional
    @SerialName("unread_conversations")
    val unreadConversations: Boolean,

    @Optional
    @SerialName("unread_conversations_count")
    val unreadConversationsCount: Long,

    @Optional
    @SerialName("unread_messages")
    val unreadMessages: Boolean,

    @Optional
    @SerialName("unread_messages_count")
    val unreadMessagesCount: Long
)

@Serializable
data class Comment(
    @Optional
    val id: Long,
    @Optional
    val user: Owner,
    @Optional
    val content: String? = null,

    @Optional
    @SerialName("created_at")
    val createdAt: Long,

    @Optional
    @SerialName("updated_at")
    val updatedAt: Long,

//    @Optional
//    val mentions: MentionsClass,
//    @Optional
//    val tags: MentionsClass,
    @Optional
    val active: Boolean,
    @Optional
    val audio: CommentAudio,
    @Optional
    val type: String,
    @Optional
    val liked: Boolean,

    @Optional
    @SerialName("number_of_likes")
    val numberOfLikes: Long,

    @Optional
    @SerialName("owner_id")
    val ownerID: Long,

    @Optional
    @SerialName("owner_type")
    val ownerType: String,

    @Optional
    val comments: JsonArray,
    @Optional
    val podcast: NWPodcast? = NWPodcast(),

    @Optional
    @SerialName("number_of_listens")
    val numberOfListens: Long,

    @Optional
    @SerialName("podcast_id")
    val podcastID: Int = 0,

    @Optional
    val ad: Ad? = null,
    @Optional
    val links: DataLinks,

    @Optional
    @SerialName("comment_count")
    val commentCount: Long
)

@Serializable
data class CommentAudio(
    @Optional
    val url: String,
    @Optional
    val duration: Long
)

@Serializable
data class Owner(
    @Optional
    val id: Long,
    @Optional
    val username: String,

    @Optional
    @SerialName("first_name")
    val firstName: String,

    @Optional
    @SerialName("last_name")
    val lastName: String,

    @Optional
    val images: Images,
    @Optional
    val blocked: Boolean,
    @Optional
    val followed: Boolean,

    @Optional
    @SerialName("blocked_by")
    val blockedBy: Boolean,

    @Optional
    @SerialName("followed_by")
    val followedBy: Boolean,

    @Optional
    val email: String,

    @Optional
    @SerialName("following_count")
    val followingCount: Long,

    @Optional
    @SerialName("followers_count")
    val followersCount: Long,

    @Optional
    val description: String,
    @Optional
    val website: String,
    @Optional
    val gender: String,

    @Optional
    @SerialName("date_of_birth")
    val dateOfBirth: Long,

    @Optional
    @SerialName("phone_number")
    val phoneNumber: String,

    @Optional
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean,

    @Optional
    val active: Boolean,
    @Optional
    val suspended: Boolean,
    @Optional
    val verified: Boolean,

    @Optional
    @SerialName("autoplay_enabled")
    val autoplayEnabled: Boolean,

    @Optional
    @SerialName("sharing_url")
    val sharingURL: String,

    @Optional
    val links: DataLinks,

    @Optional
    @SerialName("unread_conversations")
    val unreadConversations: Boolean,

    @Optional
    @SerialName("unread_conversations_count")
    val unreadConversationsCount: Long,

    @Optional
    @SerialName("unread_messages")
    val unreadMessages: Boolean,

    @Optional
    @SerialName("unread_messages_count")
    val unreadMessagesCount: Long
)

@Serializable
data class DataLinks(
    @Optional
    val content: List<ContentLink>,
    @Optional
    val text: List<TextLink>,
    @Optional
    val caption: List<CaptionLink>
)

//@Serializable
//data class Caption (
//    val id: Long,
//    val link: String,
//
//    @SerialName("start_index")
//    val startIndex: Long,
//
//    @SerialName("end_index")
//    val endIndex: Long
//)

@Serializable
data class ContentLink(
    @Optional
    val id: Long,
    @Optional
    val link: String,

    @SerialName("start_index")
    @Optional
    val startIndex: Long,

    @SerialName("end_index")
    @Optional
    val endIndex: Long
)

@Serializable
data class CaptionLink(
    @Optional
    val id: Long,
    @Optional
    val link: String,

    @Optional
    @SerialName("start_index")
    val startIndex: Long,

    @Optional
    @SerialName("end_index")
    val endIndex: Long
)

@Serializable
data class TextLink(
    @Optional
    val id: Long,
    @Optional
    val link: String,

    @Optional
    @SerialName("start_index")
    val startIndex: Long,

    @Optional
    @SerialName("end_index")
    val endIndex: Long
)