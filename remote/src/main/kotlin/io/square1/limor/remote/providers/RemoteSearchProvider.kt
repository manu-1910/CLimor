package io.square1.limor.remote.providers

import entities.request.DataLocationsRequest
import entities.request.DataTagsRequest
import entities.response.LocationsResponseEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.mappers.asRemoteEntity
import io.square1.limor.remote.services.search.SearchServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemoteSearchProvider
import javax.inject.Inject


@ImplicitReflectionSerializer
class RemoteSearchProviderImp @Inject constructor(private val provider: SearchServiceImp) : RemoteSearchProvider {

    override fun searchTag(dataTagsRequest: DataTagsRequest): Single<TagsResponseEntity>? {
        return provider.searchTag(dataTagsRequest.asRemoteEntity())?.asDataEntity()
    }


    override fun searchLocations(dataLocationsRequest: DataLocationsRequest): Single<LocationsResponseEntity>? {
        return provider.searchLocations(dataLocationsRequest.asRemoteEntity())?.asDataEntity()
    }

}


