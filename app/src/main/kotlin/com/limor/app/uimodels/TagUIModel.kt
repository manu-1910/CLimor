package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagUIModel(
    val id: Int,
    val tag: String,
    val startIndex: Int = -1,
    val endIndex: Int = -1,
    val count: Int = -1
): Parcelable

fun GetFeaturedCastsQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetTopCastsQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun SearchHashtagsQuery.SearchTag.mapToUIModel() =
    TagUIModel(
        id = id!!,
        tag = text!!,
        count = count!!
    )

fun GetPodcastsByCategoryQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetPodcastsByHashtagQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetUserPodcastsQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun FeedItemsQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetCommentsByPodcastsQuery.Caption1.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetCommentsByPodcastsQuery.Caption3.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetCommentsByIdQuery.Caption1.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetCommentsByIdQuery.Caption3.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )

fun GetPodcastByIdQuery.Caption.mapToUIModel() =
    TagUIModel(
        id = tag_id!!,
        tag = tag!!,
        startIndex = start_index!!,
        endIndex = end_index!!
    )