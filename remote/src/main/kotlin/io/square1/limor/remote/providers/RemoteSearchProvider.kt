package io.square1.limor.remote.providers

import entities.request.DataSearchTermRequest
import entities.response.LocationsResponseEntity
import entities.response.GetPodcastsResponseEntity
import entities.response.PromotedTagsResponseEntity
import entities.response.SuggestedUsersResponseEntity
import entities.response.TagsResponseEntity
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

    override fun searchLocations(dataSearchTermRequest: DataSearchTermRequest): Single<LocationsResponseEntity>? {
        return provider.searchLocations(dataSearchTermRequest.asRemoteEntity())?.asDataEntity()
    }

    override fun trendingTags(): Single<TagsResponseEntity>? {
        return provider.trendingTags()?.asDataEntity()
    }

    override fun promotedTags(): Single<PromotedTagsResponseEntity>? {
        return provider.promotedTags()?.asDataEntity()
    }

    override fun podcastsTag(limit: Int, offset: Int, tag: String): Single<GetPodcastsResponseEntity>? {
        return provider.podcastsByTag(limit, offset, tag)?.asDataEntity()
    }

    override fun getSuggestedUsers(): Single<SuggestedUsersResponseEntity>? {
        return provider.getSuggestedUsers()?.asDataEntity()
    }

    override fun searchUsers(searchTermRequest: DataSearchTermRequest): Single<SuggestedUsersResponseEntity>? {
        return provider.searchUsers(searchTermRequest.asRemoteEntity())?.asDataEntity()
    }

}


