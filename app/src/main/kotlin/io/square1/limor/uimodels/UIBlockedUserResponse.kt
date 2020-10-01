package io.square1.limor.uimodels

data class UIBlockedUserResponse (
    var code: Int,
    var message: String,
    var data: UIUserBlocked
)

data class UIUserBlocked (
    var blocked: Boolean
)

