package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.PatronCategoriesQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PatronCategoryUIModel(
    val id: Int?,
    val slug: String?,
    val name: String?,
    val priority: Int?,
    var selected: Boolean?
) : Parcelable

fun PatronCategoriesQuery.GetPatronCategory.mapToUIModel() =
    PatronCategoryUIModel(
        id = id!!,
        slug = slug,
        name = name,
        priority = priority,
        selected = selected
    )