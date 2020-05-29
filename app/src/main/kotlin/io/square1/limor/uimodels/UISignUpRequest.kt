package io.square1.limor.uimodels

data class UISignUpRequest(
    var client_id: String,
    var client_secret: String,
    var scopes: String = "user",
    var user: UISignUpUser
)

data class UISignUpUser(
    var email: String,
    var password: String,
    var username: String
)