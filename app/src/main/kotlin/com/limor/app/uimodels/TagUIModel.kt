package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetFeaturedCastsQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagUIModel(
    val id: Int,
    val tag: String,
    val startIndex: Int,
    val endIndex: Int
): Parcelable

fun GetFeaturedCastsQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )