package com.limor.app.uimodels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryUIModel(
    val id: Int,
    val slug: String?,
    val name: String
) : Parcelable