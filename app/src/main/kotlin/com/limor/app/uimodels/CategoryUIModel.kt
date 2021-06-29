package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.CategoriesQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryUIModel(
    val id: Int,
    val slug: String?,
    val name: String
) : Parcelable

fun CategoriesQuery.Category.mapToUIModel() =
    CategoryUIModel(
        id = id!!,
        slug = slug!!,
        name = name!!
    )