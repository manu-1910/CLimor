package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWUpdatedResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWUpdatedData? = NWUpdatedData()
)

@Serializable
data class NWUpdatedData(
    @Optional
    val updated : Boolean = false
)