package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWPublishResponse(

    val code: Int = 0,

    val data: NWDataPublishResponse = NWDataPublishResponse(),

    val message: String = ""
)

@Serializable
data class NWDataPublishResponse(

    val podcast: NWPodcast = NWPodcast()
)

@Serializable
data class NWPodcast(

    val active: Boolean = false,

    val address: String? = "",

    val audio: NWAudio = NWAudio(),

    val bookmarked: Boolean = false,

    val caption: String = "",

    val created_at: Int = 0,

    val id: Int = 0,

    val images: NWImages = NWImages(),

    val latitude: Double? = 0.0,

    val liked: Boolean = false,

    val links: NWLinks = NWLinks(),

    val listened: Boolean = false,

    val longitude: Double? = 0.0,

    val mentions: NWMentions = NWMentions(),

    val number_of_comments: Int = 0,

    val number_of_likes: Int = 0,

    val number_of_listens: Int = 0,

    val number_of_recasts: Int = 0,

    val recasted: Boolean = false,

    val reported: Boolean = false,

    val saved: Boolean = false,

    val sharing_url: String? = "",

    val tags: NWTagsArray = NWTagsArray(),

    val title: String = "",

    val updated_at: Int = 0,

    val user: NWUser = NWUser(),

    val category: NWCategory? = NWCategory()
)


@Serializable
data class NWAudio(

    val audio_url: String = "",

    val original_audio_url: String? = "",

    val duration: Int = 0,

    val total_samples: Double = 0.0,

    val total_length: Double = 0.0
)

@Serializable
class NWMentions(

    val mentions : NWContentMentionItemsArray = NWContentMentionItemsArray()
)

@Serializable
class NWContentMentionItemsArray (

    val content : ArrayList<NWContentMentionItem> = ArrayList()
)

@Serializable
class NWContentMentionItem(

    val user_id : Int = 0,

    val username : String = "",

    val start_index : Int = 0,

    val end_index : Int = 0
)

@Serializable
class NWTags(

    val id: Int = 0,

    val text: String = "",

    val count: Int = 0,

    val isSelected: Boolean = false
)


@Serializable
class NWCategory(

    val id: Int = 0,

    val name: String = "",

    val priority: Int = 0,

    val created_at: Long = 0,

    val updated_at: Long = 0
)