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
        content = content!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        }
    )

fun GetTopCastsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        }
    )

fun GetPodcastsByCategoryQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        }
    )

fun GetPodcastsByHashtagQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        }
    )

fun GetUserPodcastsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        }
    )

fun FeedItemsQuery.Mentions.mapToUIModel() =
    MentionUIModel(
        content = content!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        },
        caption = caption!!.map {
            MentionUIModel.MentionDataUIModel(
                userId = it!!.user_id!!,
                username = it.username!!,
                startIndex = it.start_index!!,
                endIndex = it.end_index!!,
            )
        }
    )