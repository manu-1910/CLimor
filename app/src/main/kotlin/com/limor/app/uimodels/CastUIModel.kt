package com.limor.app.uimodels

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Parcelable
import com.limor.app.*
import com.limor.app.dm.ShareResult
import com.limor.app.extensions.toLocalDateTime
import com.limor.app.scenes.utils.DateUiUtil
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime
import java.util.*

@Parcelize
data class PatronDetails(
    val priceId: String?,
    var previewDuration: Int?,
    var startsAt: Int?,
    var endsAt: Int?,
    val purchased: Boolean?
) : Parcelable {

}

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
    val podcastCreatedAt: LocalDateTime?,
    val latitude: Float?,
    val longitude: Float?,
    val isLiked: Boolean?,
    val isReported: Boolean?,
    val isRecasted: Boolean?,
    val isListened: Boolean?,
    var isShared: Boolean?,
    val isBookmarked: Boolean?,
    val listensCount: Int?,
    val likesCount: Int?,
    val recastsCount: Int?,
    val commentsCount: Int?,
    var sharesCount: Int?,
    val audio: AudioUIModel?,
    val isActive: Boolean?,
    val sharingUrl: String?,
    val tags: List<TagUIModel>?,
    val mentions: MentionUIModel?,
    val links: LinkUIModel?,
    val recaster: UserUIModel?,
    var colorCode: String? = null,
    val maturedContent: Boolean?,
    val patronCast: Boolean?,
    val patronDetails: PatronDetails? = null
) : Parcelable {

    /**
     * X days ago - Berlin
     * Today - Berlin
     */
    fun getCreationDateAndPlace(context: Context, activePodcast: Boolean): String {
        var location = address
        if (location.isNullOrEmpty()) {
            location = getLocation(context)
        }
        return "${
            if (activePodcast) {
                podcastCreatedAt?.let {
                    DateUiUtil.getTimeAgoText(
                        podcastCreatedAt,
                        context
                    )
                }
            } else {
                createdAt?.let {
                    DateUiUtil.getTimeAgoText(
                        createdAt,
                        context
                    )
                }
            }
        } ${if (!activePodcast || location.isNullOrEmpty()) "" else " - $location"}"
    }

    private fun getLocation(context: Context): String {
        if (latitude != null && longitude != null) {
            val geoCoder = Geocoder(context, Locale.getDefault()) //it is Geocoder
            try {
                val address: List<Address> = geoCoder.getFromLocation(
                    latitude.toDouble(),
                    longitude.toDouble(),
                    1
                )
                // This CAN be null (check the sources)
                if (null == address || address.isEmpty()) {
                    return ""
                }
                return address[0].locality
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return ""
    }

    fun updateShares(shareResult: ShareResult) {
        isShared = (isShared ?: false) || shareResult.hasShared
        sharesCount = (sharesCount ?: 0) + shareResult.newSharesCount
    }
}

fun GetFeaturedCastsQuery.GetFeaturedCast.mapToUIModel() =
    CastUIModel(
        id = id!!,
        owner = owner?.mapToUIModel(),
        title = title,
        address = address,
        recasted = false,
        imageLinks = images?.mapToUIModel(),
        caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        podcastCreatedAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(),
        longitude = longitude?.toFloat(),
        isLiked = liked,
        isReported = reported,
        isRecasted = recasted,
        isListened = listened,
        isShared = false,
        isBookmarked = bookmarked,
        listensCount = number_of_listens,
        likesCount = number_of_likes,
        recastsCount = number_of_recasts,
        commentsCount = number_of_comments,
        sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(),
        isActive = active,
        sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() },
        mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
        recaster = null,
        colorCode = color_code,
        maturedContent = false, 
        patronCast = false
    )

fun GetTopCastsQuery.GetTopCast.mapToUIModel() =
    CastUIModel(
        id = id!!,
        owner = owner?.mapToUIModel(),
        title = title,
        address = address,
        recasted = false,
        imageLinks = images?.mapToUIModel(),
        caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        podcastCreatedAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(),
        longitude = longitude?.toFloat(),
        isLiked = liked,
        isReported = reported,
        isRecasted = recasted,
        isListened = listened,
        isShared = false,
        isBookmarked = bookmarked,
        listensCount = number_of_listens,
        likesCount = number_of_likes,
        recastsCount = number_of_recasts,
        commentsCount = number_of_comments,
        sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(),
        isActive = active,
        sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() },
        mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
        recaster = null,
        colorCode = color_code,
        maturedContent = false, 
        patronCast = false
    )

fun GetPodcastsByCategoryQuery.GetPodcastsByCategory.mapToUIModel() =
    CastUIModel(
        id = id!!,
        owner = owner?.mapToUIModel(),
        title = title,
        address = address,
        recasted = false,
        imageLinks = images?.mapToUIModel(),
        caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        podcastCreatedAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),

        latitude = latitude?.toFloat(),
        longitude = longitude?.toFloat(),
        isLiked = liked,
        isReported = reported,
        isRecasted = recasted,
        isListened = listened,
        isShared = false,
        isBookmarked = bookmarked,
        listensCount = number_of_listens,
        likesCount = number_of_likes,
        recastsCount = number_of_recasts,
        commentsCount = number_of_comments,
        sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(),
        isActive = active,
        sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() },
        mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
        recaster = null,
        colorCode = color_code,
        maturedContent = false,
        patronCast = false
    )

fun GetPodcastsByHashtagQuery.GetPodcastsByTag.mapToUIModel() =
    CastUIModel(
        id = id!!,
        owner = owner?.mapToUIModel(),
        title = title,
        address = address,
        recasted = false,
        imageLinks = images?.mapToUIModel(),
        caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        podcastCreatedAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(),
        longitude = longitude?.toFloat(),
        isLiked = liked,
        isReported = reported,
        isRecasted = recasted,
        isListened = listened,
        isShared = false,
        isBookmarked = bookmarked,
        listensCount = number_of_listens,
        likesCount = number_of_likes,
        recastsCount = number_of_recasts,
        commentsCount = number_of_comments,
        sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(),
        isActive = active,
        sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() },
        mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
        recaster = null,
        colorCode = color_code,
        maturedContent = false, 
        patronCast = false
    )

fun GetUserPodcastsQuery.GetUserPodcast.mapToUIModel() =
    CastUIModel(
        id = id!!,
        owner = owner?.mapToUIModel(),
        title = title,
        address = address,
        recasted = false,
        imageLinks = images?.mapToUIModel(),
        caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        podcastCreatedAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(),
        longitude = longitude?.toFloat(),
        isLiked = liked,
        isShared = false,
        isReported = reported,
        isRecasted = recasted,
        isListened = listened,
        isBookmarked = bookmarked,
        listensCount = number_of_listens,
        likesCount = number_of_likes,
        recastsCount = number_of_recasts,
        commentsCount = number_of_comments,
        sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(),
        isActive = active,
        sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() },
        mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
        recaster = null,
        colorCode = color_code,
        maturedContent = false, 
        patronCast = false
    )

fun GetPatronPodcastsQuery.GetPatronCast.mapToUIModel() =
    CastUIModel(
        id = id!!, owner = owner?.mapToUIModel(), title = title, address = address, recasted = false,
        imageLinks = images?.mapToUIModel(), caption = caption!!,
        createdAt = created_at?.toLocalDateTime(), podcastCreatedAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(), longitude = longitude?.toFloat(), isLiked = liked, isShared = false,
        isReported = reported, isRecasted = recasted, isListened = listened,
        isBookmarked = bookmarked, listensCount = number_of_listens,
        likesCount = number_of_likes, recastsCount = number_of_recasts,
        commentsCount = number_of_comments, sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(), isActive = active, sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() }, mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(), recaster = null,
        maturedContent = false, 
        patronCast = false
    )

fun FeedItemsQuery.GetFeedItem.mapToUIModel() =
    CastUIModel(
        id = podcast!!.id!!,
        owner = podcast.owner?.mapToUIModel(),
        title = podcast.title,
        address = podcast.address,
        recasted = recasted,
        imageLinks = podcast.images?.mapToUIModel(),
        caption = podcast.caption,
        createdAt = created_at?.toLocalDateTime(),
        podcastCreatedAt = podcast.created_at?.toLocalDateTime(),
        updatedAt = podcast.updated_at?.toLocalDateTime(),
        latitude = podcast.latitude?.toFloat(),
        longitude = podcast.longitude?.toFloat(),
        isLiked = podcast.liked,
        isShared = false,
        isReported = podcast.reported,
        isRecasted = podcast.recasted,
        isListened = podcast.listened,
        isBookmarked = podcast.bookmarked,
        listensCount = podcast.number_of_listens,
        likesCount = podcast.number_of_likes,
        recastsCount = podcast.number_of_recasts,
        commentsCount = podcast.number_of_comments,
        sharesCount = podcast.number_of_shares,
        audio = podcast.audio?.mapToUIModel(),
        isActive = podcast.active,
        sharingUrl = podcast.sharing_url,
        tags = podcast.tags?.caption?.map { it!!.mapToUIModel() },
        mentions = podcast.mentions?.mapToUIModel(),
        links = podcast.links?.mapToUIModel(),
        recaster = recaster?.mapToUIModel(),
        colorCode = podcast.color_code,
        maturedContent = podcast.mature_content, 
        patronCast = podcast.patron_cast,
        patronDetails = podcast.patron_details?.let {
            PatronDetails(
                priceId = it.price_id,
                previewDuration = it.preview_duration,
                startsAt = it.starts_at,
                endsAt = it.ends_at,
                purchased = it.purchased
            )
        }
    )

fun GetPodcastByIdQuery.GetPodcastById.mapToUIModel() =
    CastUIModel(
        id = id!!,
        owner = owner?.mapToUIModel(),
        title = title,
        address = address,
        recasted = false,
        imageLinks = images?.mapToUIModel(),
        caption = caption!!,
        createdAt = created_at?.toLocalDateTime(),
        podcastCreatedAt = created_at?.toLocalDateTime(),
        updatedAt = updated_at?.toLocalDateTime(),
        latitude = latitude?.toFloat(),
        longitude = longitude?.toFloat(),
        isLiked = liked,
        isReported = reported,
        isRecasted = recasted,
        isListened = listened,
        isShared = false,
        isBookmarked = bookmarked,
        listensCount = number_of_listens,
        likesCount = number_of_likes,
        recastsCount = number_of_recasts,
        commentsCount = number_of_comments,
        sharesCount = number_of_shares,
        audio = audio?.mapToUIModel(),
        isActive = active,
        sharingUrl = sharing_url,
        tags = tags?.caption?.map { it!!.mapToUIModel() },
        mentions = mentions?.mapToUIModel(),
        links = links?.mapToUIModel(),
        recaster = null,
        colorCode = color_code,
        maturedContent = false, 
        patronCast = false
    )
