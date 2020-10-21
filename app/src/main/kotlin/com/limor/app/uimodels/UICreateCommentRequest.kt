package com.limor.app.uimodels

data class UICreateCommentRequest(
    var comment: UICommentRequest
)

data class UICommentRequest(
    var content: String,
    var duration: Int?,
    var audio_url: String?
)