package io.square1.limor.remote.entities.responses



import kotlinx.serialization.Serializable


@Serializable
data class NWPromotedTagsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWPromotedTagsArray = NWPromotedTagsArray()
)

@Serializable
data class NWPromotedTagsArray(

    val promoted_tags: ArrayList<NWTags> = ArrayList()
)

