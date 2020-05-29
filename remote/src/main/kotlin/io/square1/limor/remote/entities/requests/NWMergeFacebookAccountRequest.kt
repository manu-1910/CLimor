package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWMergeFacebookAccountRequest(
    var facebook_uid: String,
    var facebook_access_token: String
)
