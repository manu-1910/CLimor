package com.limor.app.uimodels;

import android.os.Parcelable
import com.limor.app.FeaturedPodcastsGroupsQuery
import kotlinx.android.parcel.Parcelize;

@Parcelize
data class FeaturedPodcastGroups(
    val count: Int,
    val podcastGroups: List<PodcastGroup>
): Parcelable

@Parcelize
data class PodcastGroup(
    val id: Int,
    val position: Int,
    val title: String?
): Parcelable

fun FeaturedPodcastsGroupsQuery.Data1.mapToUIModel(): FeaturedPodcastGroups{
    val podcastGroups = mutableListOf<PodcastGroup>()
    featuredPodcastGroups.map { it?.let {
        podcastGroups.add(PodcastGroup(it.id, it.position, it.title))
    }}
    podcastGroups.sortBy { it.position }
    return FeaturedPodcastGroups(
        count, podcastGroups
    )
}