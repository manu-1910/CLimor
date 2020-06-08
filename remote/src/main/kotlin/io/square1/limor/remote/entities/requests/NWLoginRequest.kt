package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWLoginRequest(
    var client_id: String = "",
    var client_secret: String = "",
    var grant_type: String = "",
    var scopes: String = "",
    var username: String = "",
    var password: String = ""
)
