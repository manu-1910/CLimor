package io.square1.limor.remote.entities.responses


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NWErrorResponse(

    @SerialName("code")
    var code: Int = 0,

    @SerialName("message")
    var messageStr: String? = ""
    //
    //@SerialName("data")
    //var data: String = ""
): Throwable()