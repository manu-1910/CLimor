package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetCommentsByIdQuery
import com.limor.app.GetCommentsByPodcastsQuery
import kotlinx.android.parcel.Parcelize
import java.time.Duration

@Parcelize
data class AudioCommentUIModel(
    val url: String?,
    val duration: Duration?
): Parcelable

fun GetCommentsByPodcastsQuery.Audio.mapToUIModel() =
    AudioCommentUIModel(
        url = url,
        duration = duration?.let { Duration.ofSeconds(it.toLong()) }
    )

fun GetCommentsByPodcastsQuery.Audio1.mapToUIModel() =
    AudioCommentUIModel(
        url = url,
        duration = duration?.let { Duration.ofSeconds(it.toLong()) }
    )

fun GetCommentsByIdQuery.Audio1.mapToUIModel() =
    AudioCommentUIModel(
        url = url,
        duration = duration?.let { Duration.ofSeconds(it.toLong()) }
    )

fun GetCommentsByIdQuery.Audio.mapToUIModel() =
    AudioCommentUIModel(
        url = url,
        duration = duration?.let { Duration.ofSeconds(it.toLong()) }
    )