package repositories.search

import entities.request.DataSearchTermRequest
import entities.response.LocationsResponseEntity
import entities.response.GetPodcastsResponseEntity
import entities.response.PromotedTagsResponseEntity
import entities.response.TagsResponseEntity
import entities.response.SuggestedUsersResponseEntity
import io.reactivex.Single

interface SearchRepository {
    fun searchTag(tag: String): Single<TagsResponseEntity>?
    fun searchLocations(dataSearchTermRequest: DataSearchTermRequest): Single<LocationsResponseEntity>?
    fun trendingTags(): Single<TagsResponseEntity>?
    fun promotedTags(): Single<PromotedTagsResponseEntity>?
    fun podcastsByTag(limit: Int, offset: Int, tag: String): Single<GetPodcastsResponseEntity>?
    fun getSuggestedUsers(): Single<SuggestedUsersResponseEntity>?
    fun searchUsers(dataSearchTermRequest: DataSearchTermRequest): Single<SuggestedUsersResponseEntity>?
}