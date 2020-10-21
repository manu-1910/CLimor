package com.limor.app.uimodels

data class UICreateCommentResponse (
    var code: Int,
    var message: String,
    var data: UIDataCreateComment?
)

data class UIDataCreateComment (
    var comment: UIComment
)