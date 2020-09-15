package io.square1.limor.uimodels


data class UICreateCommentReportResponse (
    var code: Int,
    var message: String,
    var data: UIReported?
)

data class UIReported (
    var reported: Boolean
)