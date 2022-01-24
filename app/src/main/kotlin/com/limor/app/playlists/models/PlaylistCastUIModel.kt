package com.limor.app.playlists.models

import com.limor.app.GetCastsInPlaylistsQuery
import com.limor.app.extensions.toLocalDateTime
import java.time.LocalDateTime

data class PlaylistCastUIModel(
    val id: Int,
    val title: String,
    val userName: String,
    val userId: Int,
    val totalLength: Double,
    val images: PlaylistImages? = null,
    val colorCode: String? = null,
    val isPatronCast: Boolean,
    val isPurchased: Boolean,
    val createdAt: String,
    val addedAt: String
)

fun GetCastsInPlaylistsQuery.Data1.mapToUIModel() =
    PlaylistCastUIModel(
        id = podcastId ?: -1,
        title = title ?: "",
        userName = username ?: "",
        userId = userId ?: -1,
        images = images?.mapToUIModel(),
        totalLength = totalLength ?: 0.0,
        colorCode = colorCode,
        isPatronCast = isPatronCast ?: false,
        isPurchased = isPurchased ?: false,
        createdAt = createdAt ?: "",
        addedAt = addedAt ?: ""
    )

fun GetCastsInPlaylistsQuery.Images.mapToUIModel() =
    PlaylistImages(
        smallUrl = small_url,
        mediumUrl = medium_url,
        largeUrl = large_url,
        originalUrl = original_url
    )