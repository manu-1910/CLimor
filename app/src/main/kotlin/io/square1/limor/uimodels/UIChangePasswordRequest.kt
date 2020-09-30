package io.square1.limor.uimodels

data class UIChangePasswordRequest(
    var current_password: String,
    var new_password: String
)