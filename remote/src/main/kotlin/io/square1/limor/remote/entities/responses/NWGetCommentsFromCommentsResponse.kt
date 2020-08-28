package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWGetCommentsFromCommentsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWCommentsArray = NWCommentsArray()
)

//@Serializable
//data class NWCommentsArray(
//    @Optional
//    val comments: ArrayList<NWComment> = ArrayList()
//)
//
//@Serializable
//data class NWComment(
//    @Optional
//    val id: Int = 0,
//    @Optional
//    val user: NWUser = NWUser(),
//    @Optional
//    val content: String? = "",
//    @Optional
//    val created_at: Int = 0,
//    @Optional
//    val updated_at: Int = 0,
//    @Optional
//    val mentions: NWMentions = NWMentions(),
//    @Optional
//    val tags: NWTags = NWTags(),
//    @Optional
//    val active: Boolean = true,
//    @Optional
//    val audio: NWCommentAudio = NWCommentAudio(),
//    @Optional
//    val type: String = "",
//    @Optional
//    val liked: Boolean = true,
//    @Optional
//    val number_of_likes: Int = 0,
//    @Optional
//    val owner_id: Int = 0,
//    @Optional
//    val owner_type: String = "",
//    @Optional
//    val comments: ArrayList<NWComment> = ArrayList(),
//    @Optional
//    val podcast: NWPodcast? = NWPodcast(),
//    @Optional
//    val number_of_listens: Int = 0,
//    @Optional
//    val podcast_id: Int = 0,
//    @Optional
//    val links: NWLinks = NWLinks(),
//    @Optional
//    val comment_count: Int = 0
//    //@Optional
//    //val ad: ,
//)
//
//@Serializable
//data class NWCommentAudio(
//    @Optional
//    val url: String? = "",
//    @Optional
//    val duration: Int? = 0
//)
//
