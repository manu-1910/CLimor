package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.DeleteRecastMutation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeleteRecastUIModel (
    val podcastId: Int,
    val count: Int,
    val recasted: Boolean,
    val destroyed: Boolean
): Parcelable
fun DeleteRecastMutation.DeleteRecast.mapToUIModel() =
    DeleteRecastUIModel(
        podcastId = podcast_id ?: -1,
        count = count ?: 0,
        recasted = recasted ?: false,
        destroyed = destroyed ?: false
    )
