package com.limor.app.uimodels

data class UIUsernameResponse(
    val code: Int,
    val message: String,
    val data: UIUsernameDataResponse
)

data class UIUsernameDataResponse(
    val available: Boolean
)
