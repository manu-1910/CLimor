package repositories.search


import entities.request.DataLocationsRequest
import entities.response.*
import io.reactivex.Single
import providers.remote.RemoteSearchProvider
import javax.inject.Inject


class DataSearchRepository @Inject constructor(private val remoteProvider: RemoteSearchProvider) :
    SearchRepository {


    override fun searchTag(tag: String): Single<TagsResponseEntity>? {
        return remoteProvider.searchTag(tag)
    }

    override fun searchLocations(dataLocationsRequest: DataLocationsRequest): Single<LocationsResponseEntity>? {
        return remoteProvider.searchLocations(dataLocationsRequest)
    }

    override fun trendingTags(): Single<TagsResponseEntity>? {
        return remoteProvider.trendingTags()
    }

    override fun promotedTags(): Single<PromotedTagsResponseEntity>? {
        return remoteProvider.promotedTags()
    }

    override fun podcastsByTag(limit: Int, offset: Int, tag: String): Single<PodcastsByTagResponseEntity>? {
        return remoteProvider.podcastsTag(limit, offset, tag)
    }

    override fun getSuggestedUsers(): Single<SuggestedUsersResponseEntity>? {
        return remoteProvider.getSuggestedUsers()
    }

}