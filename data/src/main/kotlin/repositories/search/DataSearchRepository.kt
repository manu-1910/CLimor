package repositories.search


import entities.request.DataLocationsRequest
import entities.request.DataTagsRequest
import entities.response.LocationsResponseEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single
import providers.remote.RemoteSearchProvider
import javax.inject.Inject


class DataSearchRepository @Inject constructor(private val remoteProvider: RemoteSearchProvider): SearchRepository {


    override fun searchTag(dataTagsRequest: DataTagsRequest): Single<TagsResponseEntity>? {
        return remoteProvider.searchTag(dataTagsRequest)
    }


    override fun searchLocations(dataLocationsRequest: DataLocationsRequest): Single<LocationsResponseEntity>? {
        return remoteProvider.searchLocations(dataLocationsRequest)
    }


}