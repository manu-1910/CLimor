package io.square1.limor.remote.entities.requests

import kotlinx.serialization.Serializable

@Serializable
data class NWCreateUserReportRequest(
    var reason: String? = ""
)
