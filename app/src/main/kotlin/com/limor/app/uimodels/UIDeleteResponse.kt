package com.limor.app.uimodels

data class UIDeleteResponse (
    var code: Int,
    var message: String,
    var data: UIDeleteData?
)

data class UIDeleteData(
    var destroyed: Boolean?
)