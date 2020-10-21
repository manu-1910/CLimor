package com.limor.app.uimodels

data class UIUpdatedResponse (
    var code: Int,
    var message: String,
    var data: UIUpdatedData?
)

data class UIUpdatedData(
    var updated: Boolean
)