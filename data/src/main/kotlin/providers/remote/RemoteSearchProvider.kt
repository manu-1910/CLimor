package providers.remote

import entities.request.DataTagsRequest
import entities.response.TagsResponseEntity
import io.reactivex.Single


interface RemoteSearchProvider {
    fun searchTag(dataTagsRequest: DataTagsRequest): Single<TagsResponseEntity>?
}