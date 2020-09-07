package io.square1.limor.remote.mappers


import entities.response.TagsArrayEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWTagsArray
import io.square1.limor.remote.entities.responses.NWTagsResponse


fun Single<NWTagsResponse>.asDataEntity(): Single<TagsResponseEntity>? {
    return this.map { it.asDataEntity() }
}


fun NWTagsResponse.asDataEntity(): TagsResponseEntity{
    return TagsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWTagsArray.asDataEntity(): TagsArrayEntity{
    return TagsArrayEntity(
        getAllTagsEntities(tags)
    )
}






