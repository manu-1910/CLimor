package repositories.search


import entities.request.DataTagsRequest
import entities.response.TagsResponseEntity
import io.reactivex.Single
import providers.remote.RemoteSearchProvider
import javax.inject.Inject


class DataSearchRepository @Inject constructor(private val remoteProvider: RemoteSearchProvider): SearchRepository {


    override fun searchTag(dataTagsRequest: DataTagsRequest): Single<TagsResponseEntity>? {
        return remoteProvider.searchTag(dataTagsRequest)
    }


}