package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable


@Serializable
data class NWGetCommentsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWCommentsArray = NWCommentsArray()
)

@Serializable
data class NWCommentsArray(

    val comments: ArrayList<NWComment> = ArrayList()
)

@Serializable
data class NWComment(

    val id: Int = 0,

    val user: NWUser = NWUser(),

    val content: String? = "",

    val created_at: Int = 0,

    val updated_at: Int = 0,

    val mentions: NWMentions = NWMentions(),

    val tags: NWTags = NWTags(),

    val active: Boolean = true,

    val audio: NWCommentAudio = NWCommentAudio(),

    val type: String = "",

    val liked: Boolean = true,

    val number_of_likes: Int = 0,

    val owner_id: Int = 0,

    val owner_type: String = "",

    val comments: ArrayList<NWComment> = ArrayList(),

    val podcast: NWPodcast? = NWPodcast(),

    val number_of_listens: Int = 0,

    val podcast_id: Int? = 0,

    val links: NWLinks = NWLinks(),

    val comment_count: Int = 0,

    val ad: NWAd? = NWAd()
)

@Serializable
data class NWCommentAudio(

    val url: String? = "",

    val duration: Int? = 0
)

