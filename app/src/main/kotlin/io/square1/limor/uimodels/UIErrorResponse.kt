package io.square1.limor.uimodels

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UIErrorResponse(
    @Optional
    @SerialName("code")
    var code: Int = 0,
    @Optional
    @SerialName("message")
    var errorMessage: String? = ""
    //@Optional
    //@SerialName("data")
    //var data: Object<Any>
) : Throwable()

@Serializable
data class UIErrorData(
    @Optional
    @SerialName("errors")
    var errors: ArrayList<String> = ArrayList()
)