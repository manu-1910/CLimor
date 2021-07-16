package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetCommentsByIdQuery
import com.limor.app.GetCommentsByPodcastsQuery
import com.limor.app.service.AudioService
import kotlinx.android.parcel.Parcelize
import java.time.Duration

@Parcelize
data class AudioCommentUIModel(
    val url: String,
    val duration: Duration
): Parcelable

fun GetCommentsByPodcastsQuery.Audio.mapToUIModel(): AudioCommentUIModel? {
    if (url != null && duration != null) {
        return AudioCommentUIModel(
            url = url,
            duration = Duration.ofSeconds(duration.toLong())
        )
    }
    return null
}


fun GetCommentsByPodcastsQuery.Audio1.mapToUIModel(): AudioCommentUIModel? {
    if (url != null && duration != null) {
        return AudioCommentUIModel(
            url = url,
            duration = Duration.ofSeconds(duration.toLong())
        )
    }
    return null
}

fun GetCommentsByIdQuery.Audio1.mapToUIModel(): AudioCommentUIModel? {
    if (url != null && duration != null) {
        return AudioCommentUIModel(
            url = url,
            duration = Duration.ofSeconds(duration.toLong())
        )
    }
    return null
}
fun GetCommentsByIdQuery.Audio.mapToUIModel(): AudioCommentUIModel? {
    if (url != null && duration != null) {
        return AudioCommentUIModel(
            url = url,
            duration = Duration.ofSeconds(duration.toLong())
        )
    }
    return null
}

fun AudioCommentUIModel.mapToAudioTrack(title: String? = null) =
    AudioService.AudioTrack(
        url = url,
        duration = duration,
        title = title
    )