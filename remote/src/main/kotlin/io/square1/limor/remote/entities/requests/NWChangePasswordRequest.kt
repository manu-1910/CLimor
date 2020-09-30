package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWChangePasswordRequest(
    var current_password: String,
    var new_password: String
)