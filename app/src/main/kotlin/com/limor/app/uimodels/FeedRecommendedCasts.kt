package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetFeaturedPodcastsByGroupIdQuery
import com.limor.app.scenes.main_new.fragments.DataItem
import kotlinx.android.parcel.Parcelize

class FeedRecommendedCasts(title: String, casts: List<FeaturedPodcast> = ArrayList()) :
    DataItem {
    override var itemType: DataItem.ItemType = DataItem.ItemType.FEATURED_CAST_ITEM
    val name = title
    val recommendedCasts: List<FeaturedPodcast> = casts
    val id: Long
        get() {
            var id: Long = 0
            recommendedCasts.map { cast -> id + cast.podcast.id }
            return id
        }
}

@Parcelize
data class FeaturedPodcast(
    val position: Int,
    val podcast: CastUIModel
) : Parcelable, DataItem {
    override var itemType: DataItem.ItemType
        get() = DataItem.ItemType.FEATURED_CAST_ITEM
        set(value) {}
}

fun FeedRecommendedCasts.isEqualTo(recommendedCasts: FeedRecommendedCasts): Boolean {
    if (this.recommendedCasts.size == recommendedCasts.recommendedCasts.size) {
        this.recommendedCasts.forEachIndexed { position, user ->
            if (user != recommendedCasts.recommendedCasts[position])
                return false
        }
        return true
    } else {
        return false
    }
}

fun GetFeaturedPodcastsByGroupIdQuery.Data1.mapToUIModel(): ArrayList<FeaturedPodcast> {
    val casts = ArrayList<FeaturedPodcast>()
    podcasts.map { it ->
        it?.let {
            casts.add(
                FeaturedPodcast(
                    position = it.position,
                    podcast = CastUIModel(
                        id = it.podcast.id,
                        owner = it.podcast.user?.mapToUIModel(),
                        title = it.podcast.title,
                        address = null,
                        recasted = false,
                        imageLinks = it.podcast.images?.mapToUIModel(),
                        caption = null,
                        createdAt = null,
                        podcastCreatedAt = null,
                        updatedAt = null,
                        latitude = 0.0f,
                        longitude = 0.0f,
                        isLiked = false,
                        isReported = false,
                        isRecasted = false,
                        isListened = false,
                        isShared = false,
                        isBookmarked = null,
                        listensCount = null,
                        likesCount = null,
                        recastsCount = null,
                        commentsCount = null,
                        sharesCount = null,
                        audio = null,
                        isActive = null,
                        sharingUrl = null,
                        tags = null,
                        mentions = null,
                        links = null,
                        recaster = null,
                        colorCode = it.podcast.color_code,
                        maturedContent = null,
                        patronCast = null,
                        patronDetails = null
                    )
                )
            )
        }
    }
    return casts
}