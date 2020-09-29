package io.square1.limor.uimodels

data class UIChangePasswordResponse (
    var code: Int,
    var message: String,
    var data: UIToken?
)
