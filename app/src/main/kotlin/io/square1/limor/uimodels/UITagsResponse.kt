package io.square1.limor.uimodels

import java.io.Serializable


data class UITagsResponse(
    val code: Int,
    val message: String,
    val data: UITagsArray
)

data class UITagsArray(
    val tags: ArrayList<UITags>
): Serializable

class UITags(
    val id: Int,
    val text: String,
    val count: Int,
    var isSelected: Boolean
): Serializable
