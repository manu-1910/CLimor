package com.limor.app.uimodels

import android.content.Context
import android.os.Parcelable
import com.limor.app.*
import com.limor.app.extensions.toLocalDateTime
import com.limor.app.scenes.utils.DateUiUtil
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Parcelize
data class CastUIModel(
    val id: Int,
    val owner: UserUIModel?,
    val title: String?,
    val address: String?,
    val recasted: Boolean?,
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
    val isShared: Boolean?,
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
    val links: LinkUIModel?,
    val recaster: UserUIModel?
) : Parcelable {

    /**
     * X days ago - Berlin
     * Today - Berlin
     */
    fun getCreationDateAndPlace(context: Context) = "${
        createdAt?.let {
            DateUiUtil.getPastDateDaysTextDescription(
                createdAt,
                context
            )
        }
    } - $address"
}

fun GetFeaturedCastsQuery.GetFeaturedCast.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address, recasted = false,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened, isShared = false,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(), recaster = null
    )

fun GetTopCastsQuery.GetTopCast.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address, recasted = false,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened, isShared = false,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(), recaster = null
    )

fun GetPodcastsByCategoryQuery.GetPodcastsByCategory.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address, recasted = false,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened, isShared = false,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(), recaster = null
    )

fun GetPodcastsByHashtagQuery.GetPodcastsByTag.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address, recasted = false,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened, isShared = false,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(), recaster = null
    )

fun GetUserPodcastsQuery.GetUserPodcast.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address, recasted = false,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked, isShared = false,
        isReported = reported, isRecasted = recasted, isListened = listened,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(), recaster = null
    )

fun FeedItemsQuery.GetFeedItem.mapToUIModel() =
    CastUIModel(
        id = podcast!!.id!!, owner = podcast.owner?.mapToUIModel(), title = podcast.title,
        address = podcast.address, recasted = recasted, imageLinks = podcast.images?.mapToUIModel(),
        caption = podcast.caption, createdAt = podcast.created_at?.toLocalDateTime(),
        updatedAt = podcast.updated_at?.toLocalDateTime(), latitude = podcast.latitude?.toFloat(),
        longitude = podcast.longitude?.toFloat(), isLiked = podcast.liked, isShared = false,
        isReported = podcast.reported, isRecasted = podcast.recasted,
        isListened = podcast.listened, isBookmarked = podcast.bookmarked,
        listensCount = podcast.number_of_listens, likesCount = podcast.number_of_likes,
        recastsCount = podcast.number_of_recasts, commentsCount = podcast.number_of_comments,
        sharesCount = podcast.number_of_shares, audio = podcast.audio?.mapToUIModel(),
        isActive = podcast.active, sharingUrl = podcast.sharing_url,
        tags = podcast.tags?.caption?.map { it!!.mapToUIModel() },
        mentions = podcast.mentions?.mapToUIModel(),
        links = podcast.links?.mapToUIModel(), recaster = recaster?.mapToUIModel()
    )

fun GetPodcastByIdQuery.GetPodcastById.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address, recasted = false,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked,
        isReported = reported, isRecasted = recasted, isListened = listened, isShared = false,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(), recaster = null
    )
