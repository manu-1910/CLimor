package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWTokenFBRequest(
    var client_id: String,
    var client_secret: String,
    var grant_type: String,
    var facebook_access_token: String,
    var referral_code: String,
    var user: NWSignUpUser
)
