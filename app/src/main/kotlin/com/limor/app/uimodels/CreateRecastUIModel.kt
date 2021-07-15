package com.limor.app.uimodels

import com.limor.app.CreateRecastMutation

data class CreateRecastUIModel (
    val podcastId: Int,
    val count: Int,
    val recasted: Boolean,
    val created: Boolean
)

fun CreateRecastMutation.CreateRecast.mapToUIModel() =
    CreateRecastUIModel(
        podcastId = podcast_id ?: -1,
        count = count ?: 0,
        recasted = recasted ?: false,
        created = created ?: false
    )