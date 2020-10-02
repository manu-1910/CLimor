package entities.request


data class DataSignUpFacebookRequest(
    var client_id: String,
    var client_secret: String,
    var scopes: String = "user",
    var user: DataSignUpFacebookUser
)

data class DataSignUpFacebookUser(
    var facebook_uid: String,
    var facebook_token: String,
    var email: String,
    var password: String,
    var username: String
)