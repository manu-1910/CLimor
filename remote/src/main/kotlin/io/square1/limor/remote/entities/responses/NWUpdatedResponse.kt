package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Serializable

@Serializable
data class NWUpdatedResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWUpdatedData? = NWUpdatedData()
)

@Serializable
data class NWUpdatedData(

    val updated : Boolean = false
)