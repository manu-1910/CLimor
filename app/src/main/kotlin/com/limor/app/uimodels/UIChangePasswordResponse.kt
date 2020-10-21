package com.limor.app.uimodels

data class UIChangePasswordResponse (
    var code: Int,
    var message: String,
    var data: UIToken?
)
