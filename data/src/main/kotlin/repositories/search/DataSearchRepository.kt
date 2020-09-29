package repositories.search


import entities.request.DataSearchTermRequest
import entities.response.LocationsResponseEntity
import entities.response.GetPodcastsResponseEntity
import entities.response.PromotedTagsResponseEntity
import entities.response.TagsResponseEntity
import entities.response.SuggestedUsersResponseEntity
import io.reactivex.Single
import providers.remote.RemoteSearchProvider
import javax.inject.Inject


class DataSearchRepository @Inject constructor(private val remoteProvider: RemoteSearchProvider) :
    SearchRepository {


    override fun searchTag(tag: String): Single<TagsResponseEntity>? {
        return remoteProvider.searchTag(tag)
    }

    override fun searchLocations(dataSearchTermRequest: DataSearchTermRequest): Single<LocationsResponseEntity>? {
        return remoteProvider.searchLocations(dataSearchTermRequest)
    }

    override fun trendingTags(): Single<TagsResponseEntity>? {
        return remoteProvider.trendingTags()
    }

    override fun promotedTags(): Single<PromotedTagsResponseEntity>? {
        return remoteProvider.promotedTags()
    }

    override fun podcastsByTag(limit: Int, offset: Int, tag: String): Single<GetPodcastsResponseEntity>? {
        return remoteProvider.podcastsTag(limit, offset, tag)
    }

    override fun getSuggestedUsers(): Single<SuggestedUsersResponseEntity>? {
        return remoteProvider.getSuggestedUsers()
    }

    override fun searchUsers(searchTermRequest: DataSearchTermRequest): Single<SuggestedUsersResponseEntity>? {
        return remoteProvider.searchUsers(searchTermRequest)
    }

}