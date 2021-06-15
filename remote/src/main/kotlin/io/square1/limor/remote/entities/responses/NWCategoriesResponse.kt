package io.square1.limor.remote.entities.responses



import kotlinx.serialization.Serializable


@Serializable
data class NWCategoriesResponse(

    val code: Int = 0,

    val message: String = "",

    val data: NWCategoriesArray = NWCategoriesArray()
)

@Serializable
data class NWCategoriesArray(

    val categories: ArrayList<NWCategory> = ArrayList()
)

