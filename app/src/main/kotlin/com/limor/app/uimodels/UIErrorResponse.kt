package com.limor.app.uimodels


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UIErrorResponse(

    @SerialName("code")
    var code: Int = 0,

    @SerialName("message")
    var errorMessage: String? = ""
    //
    //@SerialName("data")
    //var data: Object<Any>
) : Throwable()

@Serializable
data class UIErrorData(

    @SerialName("errors")
    var errors: ArrayList<String> = ArrayList()
)