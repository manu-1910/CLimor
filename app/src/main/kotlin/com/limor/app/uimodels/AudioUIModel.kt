package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
import kotlinx.android.parcel.Parcelize
import java.time.Duration

@Parcelize
data class AudioUIModel(
    val url: String,
    val totalLength: Int,
    val totalSamples: Int,
    val duration: Duration,
    val sampleRate: Float,
    val originalUrl: String?
) : Parcelable

fun GetFeaturedCastsQuery.Audio.mapToUIModel() =
    AudioUIModel(
        url = audio_url!!,
        totalLength = total_length!!.toInt(),
        totalSamples = total_samples!!.toInt(),
        duration = Duration.ofSeconds(duration!!.toLong()),
        sampleRate = sample_rate!!.toFloat(),
        originalUrl = original_audio_url
    )

fun GetTopCastsQuery.Audio.mapToUIModel() =
    AudioUIModel(
        url = audio_url!!,
        totalLength = total_length!!.toInt(),
        totalSamples = total_samples!!.toInt(),
        duration = Duration.ofSeconds(duration!!.toLong()),
        sampleRate = sample_rate!!.toFloat(),
        originalUrl = original_audio_url
    )

fun GetPodcastsByCategoryQuery.Audio.mapToUIModel() =
    AudioUIModel(
        url = audio_url!!,
        totalLength = total_length!!.toInt(),
        totalSamples = total_samples!!.toInt(),
        duration = Duration.ofSeconds(duration!!.toLong()),
        sampleRate = sample_rate!!.toFloat(),
        originalUrl = original_audio_url
    )

fun GetPodcastsByHashtagQuery.Audio.mapToUIModel() =
    AudioUIModel(
        url = audio_url!!,
        totalLength = total_length!!.toInt(),
        totalSamples = total_samples!!.toInt(),
        duration = Duration.ofSeconds(duration!!.toLong()),
        sampleRate = sample_rate!!.toFloat(),
        originalUrl = original_audio_url
    )

fun GetUserPodcastsQuery.Audio.mapToUIModel() =
    AudioUIModel(
        url = audio_url!!,
        totalLength = total_length!!.toInt(),
        totalSamples = total_samples!!.toInt(),
        duration = Duration.ofSeconds(duration!!.toLong()),
        sampleRate = sample_rate!!.toFloat(),
        originalUrl = original_audio_url
    )