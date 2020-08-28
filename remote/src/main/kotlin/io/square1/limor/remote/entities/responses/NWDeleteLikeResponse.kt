package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class NWDeleteLikeResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWDeleteLikeData? = NWDeleteLikeData()
)

@Serializable
data class NWDeleteLikeData(
    @Optional
    val destroyed : Boolean? = false
)