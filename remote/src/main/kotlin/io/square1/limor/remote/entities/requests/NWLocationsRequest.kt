package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWLocationsRequest(
    var term: String = ""
)
