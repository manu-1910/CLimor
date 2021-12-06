package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageLinksUIModel(
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
) : Parcelable

fun SuggestedPeopleQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetFeaturedCastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetFeaturedCastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetTopCastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetTopCastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun SearchUsersQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPodcastsByCategoryQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPodcastsByCategoryQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPodcastsByHashtagQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPodcastsByHashtagQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetUserProfileQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetUserProfileByIdQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetUserPodcastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetUserPodcastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPatronPodcastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPatronPodcastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun FeedItemsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun FeedItemsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun FeedItemsQuery.Images2.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetCommentsByPodcastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetCommentsByPodcastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetCommentsByIdQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetCommentsByIdQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPodcastByIdQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPodcastByIdQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun FriendsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, "", "", "")
}

fun FollowersQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, "", "", "")
}

fun SearchFollowersQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun SearchFollowingQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPurchasedCastsQuery.Images1.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}

fun GetPurchasedCastsQuery.Images.mapToUIModel(): ImageLinksUIModel {
    return ImageLinksUIModel(small_url, medium_url, large_url, original_url)
}