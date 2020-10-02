package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWSignUpFacebookRequest(
    var client_id: String,
    var client_secret: String,
    var scopes: String = "user",
    var user: NWSignUpFacebookUser
)

@Serializable
data class NWSignUpFacebookUser(
    var facebook_uid: String,
    var facebook_token: String,
    var email: String,
    var password: String,
    var username: String
)