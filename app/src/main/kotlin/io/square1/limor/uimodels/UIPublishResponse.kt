package io.square1.limor.uimodels


data class UIPublishResponse(
    val code: Int,
    val data: UIDataPublishResponse,
    val message: String
)

data class UIDataPublishResponse(
    val podcast: UIPodcast
)

data class UIPodcast(
    val active: Boolean,
    val address: String?,
    val audio: UIAudio,
    val bookmarked: Boolean,
    val caption: String,
    val created_at: Int,
    val id: Int,
    val images: UIImages,
    val latitude: Double?,
    val liked: Boolean,
    val links: UILinks,
    val listened: Boolean,
    val longitude: Double?,
    val mentions: UIMentions,
    val number_of_comments: Int,
    val number_of_likes: Int,
    val number_of_listens: Int,
    val number_of_recasts: Int,
    val recasted: Boolean,
    val reported: Boolean,
    val saved: Boolean,
    val sharing_url: String?,
    val tags: UITagsArray,
    val title: String,
    val updated_at: Int,
    val user: UIUser
)


//data class UIAudio(
//    val audio_url: String,
//    val duration: Int,
//    val original_audio_url: String,
//    val sample_rate: Int,
//    val timestamps: List<String>,
//    val total_length: Int,
//    val total_samples: Int
//)

class UIMentions(
)

