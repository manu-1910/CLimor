package io.square1.limor.uimodels


data class UIPromotedTagsResponse(
    val code: Int,
    val message: String,
    val data: UIPromotedTagsArray
)

data class UIPromotedTagsArray(
    val promoted_tags: ArrayList<UITags>
)

