package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWCreateReportResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWReported? = NWReported()
)

@Serializable
data class NWReported (
    val reported: Boolean = true
)