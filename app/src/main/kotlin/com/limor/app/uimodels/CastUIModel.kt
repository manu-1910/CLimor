package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.*
import com.limor.app.extensions.epochSecondToLocalDateTime
import com.limor.app.extensions.toLocalDate
import com.limor.app.extensions.toLocalDateTime
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Parcelize
data class CastUIModel(
    val id: Int,
    val owner: UserUIModel?,
    val title: String?,
    val address: String?,
    val imageLinks: ImageLinksUIModel?,
    val caption: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val latitude: Float?,
    val longitude: Float?,
    val isLiked: Boolean?,
    val isReported: Boolean?,
    val isRecasted: Boolean?,
    val isListened: Boolean?,
    val isBookmarked: Boolean?,
    val listensCount: Int?,
    val likesCount: Int?,
    val recastsCount: Int?,
    val commentsCount: Int?,
    val sharesCount: Int?,
    val audio: AudioUIModel?,
    val isActive: Boolean?,
    val sharingUrl: String?,
    val tags: List<TagUIModel>?,
    val mentions: MentionUIModel?,
    val links: LinkUIModel?
) : Parcelable

fun GetFeaturedCastsQuery.GetFeaturedCast.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
    )

fun GetTopCastsQuery.GetTopCast.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
    )

fun GetPodcastsByCategoryQuery.GetPodcastsByCategory.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
    )

fun GetPodcastsByHashtagQuery.GetPodcastsByTag.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
    )

fun GetUserPodcastsQuery.GetUserPodcast.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
    )