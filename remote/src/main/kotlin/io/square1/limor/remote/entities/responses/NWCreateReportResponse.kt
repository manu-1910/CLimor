package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable


@Serializable
data class NWCreateReportResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWReported? = NWReported()
)

@Serializable
data class NWReported (
    val reported: Boolean = true
)