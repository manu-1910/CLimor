package io.square1.limor.uimodels

import java.io.Serializable


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
    var liked: Boolean,
    val links: UILinks,
    val listened: Boolean,
    val longitude: Double?,
    val mentions: UIMentions,
    val number_of_comments: Int,
    var number_of_likes: Int,
    val number_of_listens: Int,
    val number_of_recasts: Int,
    val recasted: Boolean,
    val reported: Boolean,
    val saved: Boolean,
    val sharing_url: String?,
    val tags: UITagsArray,
    val title: String,
    val updated_at: Int,
    val user: UIUser,
    val categories: ArrayList<UICategory>
) : Serializable


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
    val mentions: UIContentMentionItemsArray
) : Serializable

class UIContentMentionItemsArray (
    val content : ArrayList<UIContentMentionItem>
) : Serializable

class UIContentMentionItem (
    val user_id: Int,
    val username: String,
    val start_index: Int,
    val end_index: Int
) : Serializable

