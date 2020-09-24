package io.square1.limor.remote.providers

import entities.request.DataLocationsRequest
import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.mappers.asDataEntity
import io.square1.limor.remote.mappers.asRemoteEntity
import io.square1.limor.remote.services.search.SearchServiceImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemoteSearchProvider
import javax.inject.Inject


@ImplicitReflectionSerializer
class RemoteSearchProviderImp @Inject constructor(private val provider: SearchServiceImp) :
    RemoteSearchProvider {

    override fun searchTag(tag: String): Single<TagsResponseEntity>? {
        return provider.searchTag(tag)?.asDataEntity()
    }

    override fun searchLocations(dataLocationsRequest: DataLocationsRequest): Single<LocationsResponseEntity>? {
        return provider.searchLocations(dataLocationsRequest.asRemoteEntity())?.asDataEntity()
    }

    override fun trendingTags(): Single<TagsResponseEntity>? {
        return provider.trendingTags()?.asDataEntity()
    }

    override fun promotedTags(): Single<PromotedTagsResponseEntity>? {
        return provider.promotedTags()?.asDataEntity()
    }

    override fun podcastsTag(limit: Int, offset: Int, tag: String): Single<PodcastsByTagResponseEntity>? {
        return provider.podcastsByTag(limit, offset, tag)?.asDataEntity()
    }

    override fun getSuggestedUsers(): Single<SuggestedUsersResponseEntity>? {
        return provider.getSuggestedUsers()?.asDataEntity()
    }

}


