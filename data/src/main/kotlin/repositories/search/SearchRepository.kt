package repositories.search


import entities.request.DataLocationsRequest
import entities.request.DataTagsRequest
import entities.response.LocationsResponseEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single

interface SearchRepository {
    fun searchTag(dataTagsRequest: DataTagsRequest): Single<TagsResponseEntity>?
    fun searchLocations(dataLocationsRequest: DataLocationsRequest): Single<LocationsResponseEntity>?
}