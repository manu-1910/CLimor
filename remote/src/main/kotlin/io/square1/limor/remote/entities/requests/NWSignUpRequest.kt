package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWSignUpRequest(
    var client_id: String,
    var client_secret: String,
    var scopes: String = "user",
    var user: NWSignUpUser
)

@Serializable
data class NWSignUpUser(
    var email: String,
    var password: String,
    var username: String
)