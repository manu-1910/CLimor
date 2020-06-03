package io.square1.limor.remote.entities.responses

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NWErrorResponse(
    @Optional
    @SerialName("code")
    var code: Int = 0,
    @Optional
    @SerialName("message")
    var messageStr: String? = ""
    //@Optional
    //@SerialName("data")
    //var data: String = ""
): Throwable()