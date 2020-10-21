package com.limor.app.uimodels


data class UIGetUserResponse(
    var code: Int,
    var message: String,
    var data: UIGetUserData
)

data class UIGetUserData(
    var user: UIUser
)
