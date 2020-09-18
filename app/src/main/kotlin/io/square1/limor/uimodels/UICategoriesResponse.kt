package io.square1.limor.uimodels

import java.io.Serializable


data class UICategoriesResponse(
    val code: Int,
    val message: String,
    val data: UICategoriesArray
) : Serializable

data class UICategoriesArray(
    val categories: ArrayList<UICategory>
) : Serializable

class UICategory(
    val id: Int,
    val name: String,
    val priority: Int,
    val created_at: Long,
    val updated_at: Long
) : Serializable
