package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetFeaturedCastsQuery
import com.limor.app.SuggestedPeopleQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageLinksUIModel(
    val small: String,
    val medium: String,
    val large: String,
    val original: String,
) : Parcelable

fun SuggestedPeopleQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetFeaturedCastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetFeaturedCastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}