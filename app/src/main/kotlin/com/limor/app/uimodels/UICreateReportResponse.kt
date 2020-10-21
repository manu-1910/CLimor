package com.limor.app.uimodels


data class UICreateReportResponse (
    var code: Int,
    var message: String,
    var data: UIReported?
)

data class UIReported (
    var reported: Boolean
)