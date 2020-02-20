package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NWErrorResponse(
    @Optional
    @SerialName("ErrorCode")
    var code: String = "",
    @Optional
    @SerialName("FieldName")
    var fieldName: String = "",
    @Optional
    @SerialName("Message")
    override var message: String = ""
): Throwable()