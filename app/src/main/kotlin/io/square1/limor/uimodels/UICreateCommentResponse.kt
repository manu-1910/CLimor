package io.square1.limor.uimodels

data class UICreateCommentResponse (
    var code: Int,
    var message: String,
    var data: UIDataCreateComment?
)

data class UIDataCreateComment (
    var comment: UIComment
)