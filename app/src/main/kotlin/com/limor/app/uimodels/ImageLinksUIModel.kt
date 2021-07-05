package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
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

fun GetTopCastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetTopCastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun SearchUsersQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetPodcastsByCategoryQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetPodcastsByCategoryQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}
fun GetPodcastsByHashtagQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetPodcastsByHashtagQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetUserProfileQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}

fun GetUserProfileByIdQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url!!, medium_url!!, large_url!!, original_url!!)
}