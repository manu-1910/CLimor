package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWUserIDRequest(
    var user_id: Int = 0
)
