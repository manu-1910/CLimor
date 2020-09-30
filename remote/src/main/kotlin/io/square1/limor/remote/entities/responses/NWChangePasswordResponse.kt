package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Serializable

@Serializable
data class NWChangePasswordResponse(
    var code: Int,
    var message: String,
    var data: NWToken
)
