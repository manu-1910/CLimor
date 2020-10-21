package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWDropOffRequest(
    var percentage: Float = 0.0f
)