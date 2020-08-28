package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWPublishResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val data: NWDataPublishResponse = NWDataPublishResponse(),
    @Optional
    val message: String = ""
)

@Serializable
data class NWDataPublishResponse(
    @Optional
    val podcast: NWPodcast = NWPodcast()
)

@Serializable
data class NWPodcast(
    @Optional
    val active: Boolean = false,
    @Optional
    val address: String? = "",
    @Optional
    val audio: NWAudio = NWAudio(),
    @Optional
    val bookmarked: Boolean = false,
    @Optional
    val caption: String = "",
    @Optional
    val created_at: Int = 0,
    @Optional
    val id: Int = 0,
    @Optional
    val images: NWImages = NWImages(),
    @Optional
    val latitude: Double? = 0.0,
    @Optional
    val liked: Boolean = false,
    @Optional
    val links: NWLinks = NWLinks(),
    @Optional
    val listened: Boolean = false,
    @Optional
    val longitude: Double? = 0.0,
    @Optional
    val mentions: NWMentions = NWMentions(),
    @Optional
    val number_of_comments: Int = 0,
    @Optional
    val number_of_likes: Int = 0,
    @Optional
    val number_of_listens: Int = 0,
    @Optional
    val number_of_recasts: Int = 0,
    @Optional
    val recasted: Boolean = false,
    @Optional
    val reported: Boolean = false,
    @Optional
    val saved: Boolean = false,
    @Optional
    val sharing_url: String? = "",
    @Optional
    val tags: NWTagsArray = NWTagsArray(),
    @Optional
    val title: String = "",
    @Optional
    val updated_at: Int = 0,
    @Optional
    val user: NWUser = NWUser()
)


@Serializable
data class NWAudio(
    @Optional
    val audio_url: String = "",
    @Optional
    val duration: Int = 0,
    @Optional
    val original_audio_url: String? = "",
    @Optional
    val sample_rate: Double = 0.0,
    @Optional
    val timestamps: ArrayList<String> = ArrayList(),
    @Optional
    val total_length: Double = 0.0,
    @Optional
    val total_samples: Double = 0.0
)

@Serializable
class NWMentions(
    @Optional
    val mentions : NWContentMentionItemsArray = NWContentMentionItemsArray()
)

@Serializable
class NWContentMentionItemsArray (
    @Optional
    val content : ArrayList<NWContentMentionItem> = ArrayList()
)

@Serializable
class NWContentMentionItem(
    @Optional
    val user_id : Int = 0,
    @Optional
    val username : String = "",
    @Optional
    val start_index : Int = 0,
    @Optional
    val end_index : Int = 0
)

@Serializable
class NWTags(
    @Optional
    val id: Int = 0,
    @Optional
    val text: String = "",
    @Optional
    val count: Int = 0,
    @Optional
    val isSelected: Boolean = false
)