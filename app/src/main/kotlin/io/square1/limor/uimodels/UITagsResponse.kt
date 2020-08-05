package io.square1.limor.uimodels


data class UITagsResponse(
    val code: Int,
    val message: String,
    val data: UITagsArray
)

data class UITagsArray(
    val tags: ArrayList<UITags>
)