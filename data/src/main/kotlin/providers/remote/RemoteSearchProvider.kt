package providers.remote

import entities.request.DataLocationsRequest
import entities.request.DataTagsRequest
import entities.response.LocationsResponseEntity
import entities.response.PromotedTagsResponseEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single


interface RemoteSearchProvider {
    fun searchTag(dataTagsRequest: DataTagsRequest): Single<TagsResponseEntity>?
    fun searchLocations(dataLocationsRequest: DataLocationsRequest): Single<LocationsResponseEntity>?
    fun trendingTags(): Single<TagsResponseEntity>?
    fun promotedTags(): Single<PromotedTagsResponseEntity>?
}