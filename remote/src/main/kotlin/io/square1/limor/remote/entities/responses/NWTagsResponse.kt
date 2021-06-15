package io.square1.limor.remote.entities.responses



import kotlinx.serialization.Serializable


@Serializable
data class NWTagsResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWTagsArray = NWTagsArray()
)

@Serializable
data class NWTagsArray(

    val tags: ArrayList<NWTags> = ArrayList()
)

