package repositories.search


import entities.request.DataTagsRequest
import entities.response.TagsResponseEntity
import io.reactivex.Single

interface SearchRepository {
    fun searchTag(dataTagsRequest: DataTagsRequest): Single<TagsResponseEntity>?
}