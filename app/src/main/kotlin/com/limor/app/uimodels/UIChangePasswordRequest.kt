package com.limor.app.uimodels

data class UIChangePasswordRequest(
    var current_password: String,
    var new_password: String
)