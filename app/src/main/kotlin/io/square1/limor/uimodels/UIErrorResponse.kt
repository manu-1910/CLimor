package io.square1.limor.uimodels

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UIErrorResponse(
    @Optional
    @SerialName("statusLead")
    var status: String = "",
    @Optional
    @SerialName("data")
    var data: UIErrorData = UIErrorData()
) : Throwable()

@Serializable
data class UIErrorData(
    @Optional
    @SerialName("errors")
    var errors: ArrayList<String> = ArrayList()
)