package io.square1.limor.remote.mappers


import entities.response.PromotedTagsArrayEntity
import entities.response.PromotedTagsResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWPromotedTagsArray
import io.square1.limor.remote.entities.responses.NWPromotedTagsResponse


fun Single<NWPromotedTagsResponse>.asDataEntity(): Single<PromotedTagsResponseEntity>? {
    return this.map { it.asDataEntity() }
}


fun NWPromotedTagsResponse.asDataEntity(): PromotedTagsResponseEntity{
    return PromotedTagsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}



fun NWPromotedTagsArray.asDataEntity(): PromotedTagsArrayEntity {
    return PromotedTagsArrayEntity(
        getAllTagsEntities(promoted_tags)
    )
}






