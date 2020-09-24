package repositories.search

import entities.request.DataLocationsRequest
import entities.response.*
import io.reactivex.Single

interface SearchRepository {
    fun searchTag(tag: String): Single<TagsResponseEntity>?
    fun searchLocations(dataLocationsRequest: DataLocationsRequest): Single<LocationsResponseEntity>?
    fun trendingTags(): Single<TagsResponseEntity>?
    fun promotedTags(): Single<PromotedTagsResponseEntity>?
    fun podcastsByTag(limit: Int, offset: Int, tag: String): Single<PodcastsByTagResponseEntity>?
    fun getSuggestedUsers(): Single<SuggestedUsersResponseEntity>?
}