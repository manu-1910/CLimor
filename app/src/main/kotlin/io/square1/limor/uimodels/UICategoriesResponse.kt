package io.square1.limor.uimodels


data class UICategoriesResponse(
    val code: Int,
    val message: String,
    val data: UICategoriesArray
)

data class UICategoriesArray(
    val categories: ArrayList<UICategory>
)

class UICategory(
    val id: Int,
    val name: String,
    val priority: Int,
    val created_at: Long,
    val updated_at: Long
)
