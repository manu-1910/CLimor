package io.square1.limor.uimodels

data class UICreateCommentRequest(
    var comment: UICommentRequest
)

data class UICommentRequest(
    var content: String,
    var duration: Int?,
    var audio_url: String?
)