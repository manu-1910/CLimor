package io.square1.limor.uimodels


data class UICreateReportResponse (
    var code: Int,
    var message: String,
    var data: UIReported?
)

data class UIReported (
    var reported: Boolean
)