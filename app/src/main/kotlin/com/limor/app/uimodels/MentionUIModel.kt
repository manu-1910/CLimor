package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MentionUIModel(
    val content: List<MentionDataUIModel>,
    val caption: List<MentionDataUIModel>,
) : Parcelable {

    @Parcelize
    data class MentionDataUIModel(
        val userId: Int,
        val username: String,
        val startIndex: Int,
        val endIndex: Int
    ) : Parcelable
}

fun GetFeaturedCastsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetTopCastsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetPodcastsByCategoryQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetPodcastsByHashtagQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetUserPodcastsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetPatronPodcastsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun FeedItemsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetCommentsByPodcastsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetCommentsByPodcastsQuery.Mentions1.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetCommentsByIdQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetCommentsByIdQuery.Mentions1.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )

fun GetPodcastByIdQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList(),
        caption = caption?.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        } ?: emptyList()
    )