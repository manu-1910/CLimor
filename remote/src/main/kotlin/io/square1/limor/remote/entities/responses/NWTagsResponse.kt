package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWTagsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWTagsArray = NWTagsArray()
)

@Serializable
data class NWTagsArray(
    @Optional
    val tags: ArrayList<NWTags> = ArrayList()
)

