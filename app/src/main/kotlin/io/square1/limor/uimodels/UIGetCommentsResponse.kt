package io.square1.limor.uimodels

import java.io.Serializable


data class UIGetCommentsResponse(
    var code: Int,
    var message: String,
    var data: UICommentsArray = UICommentsArray()
)

data class UICommentsArray(
    var comments: ArrayList<UIComment> = ArrayList()
)

data class UIComment(
    var id: Int,
    var user: UIUser?,
    var content: String,
    var created_at: Int,
    var updated_at: Int,
    var mentions: UIMentions,
    var tags: UITags,
    var active: Boolean,
    var audio: UICommentAudio,
    var type: String,
    var liked: Boolean,
    var number_of_likes: Int,
    var owner_id: Int,
    var owner_type: String,
    var comments: ArrayList<UIComment>,
    var podcast: UIPodcast?,
    var number_of_listens: Int,
    var podcast_id: Int,
    var links: UILinks,
    var comment_count: Int
) : Serializable

data class UICommentAudio(
    var url: String?,
    var duration: Int?
) : Serializable