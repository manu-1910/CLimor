package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWCategoriesResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWCategoriesArray = NWCategoriesArray()
)

@Serializable
data class NWCategoriesArray(
    @Optional
    val categories: ArrayList<NWCategory> = ArrayList()
)

