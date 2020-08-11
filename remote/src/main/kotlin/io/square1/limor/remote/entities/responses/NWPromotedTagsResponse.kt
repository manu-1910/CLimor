package io.square1.limor.remote.entities.responses


import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable


@Serializable
data class NWPromotedTagsResponse(
    @Optional
    val code: Int = 0,
    @Optional
    val message: String = "",
    @Optional
    val data: NWPromotedTagsArray = NWPromotedTagsArray()
)

@Serializable
data class NWPromotedTagsArray(
    @Optional
    val promoted_tags: ArrayList<NWTags> = ArrayList()
)

