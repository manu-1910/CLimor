package io.square1.limor.uimodels

data class UIDeleteLikeResponse (
    var code: Int,
    var message: String,
    var data: UIDeleteLikeData?
)

data class UIDeleteLikeData(
    var destroyed: Boolean?
)