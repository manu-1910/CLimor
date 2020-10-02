package io.square1.limor.uimodels


data class UISignUpFacebookRequest(
    var client_id: String,
    var client_secret: String,
    var scopes: String = "user",
    var user: UISignUpUser
)

data class UISignUpFacebookUser(
    var facebook_uid: String,
    var facebook_token: String,
    var email: String,
    var password: String,
    var username: String
)