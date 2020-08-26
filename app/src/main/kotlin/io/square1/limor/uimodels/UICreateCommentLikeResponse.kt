package io.square1.limor.uimodels

data class UICreateCommentLikeResponse (
    var code: Int,
    var message: String,
    var data: UICreateCommentLikeData?
)

data class UICreateCommentLikeData(
    var like: UICommentLike?
)

data class UICommentLike (
    var comment_id : Int,
    var user_id : Int
)