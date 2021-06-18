package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWDeleteResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWDeleteData? = NWDeleteData()
)

@Serializable
data class NWDeleteData(

    val destroyed : Boolean? = false
)