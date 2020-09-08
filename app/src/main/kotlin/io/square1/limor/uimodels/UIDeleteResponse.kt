package io.square1.limor.uimodels

data class UIDeleteResponse (
    var code: Int,
    var message: String,
    var data: UIDeleteData?
)

data class UIDeleteData(
    var destroyed: Boolean?
)