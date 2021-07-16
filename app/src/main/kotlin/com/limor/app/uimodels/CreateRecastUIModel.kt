package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.CreateRecastMutation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreateRecastUIModel (
    val podcastId: Int,
    val count: Int,
    val recasted: Boolean,
    val created: Boolean
) : Parcelable

fun CreateRecastMutation.CreateRecast.mapToUIModel() =
    CreateRecastUIModel(
        podcastId = podcast_id ?: -1,
        count = count ?: 0,
        recasted = recasted ?: false,
        created = created ?: false
    )