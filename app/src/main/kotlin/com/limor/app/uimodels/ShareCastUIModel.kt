package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.CreateRecastMutation
import com.limor.app.SharePodcastMutation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShareCastUIModel (
    val podcastId: Int,
    val count: Int,
    val shared: Boolean,
    val created: Boolean
) : Parcelable

fun SharePodcastMutation.SharePodcast.mapToUIModel() =
    ShareCastUIModel(
        podcastId = podcast_id ?: -1,
        count = count ?: 0,
        shared = shared ?: false,
        created = created ?: false
    )