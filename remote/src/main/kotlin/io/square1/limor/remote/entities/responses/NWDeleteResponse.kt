package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWDeleteResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWDeleteData? = NWDeleteData()
)

@Serializable
data class NWDeleteData(
    @Optional
    val destroyed : Boolean? = false
)