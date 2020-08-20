package io.square1.limor.mappers

import entities.response.PromotedTagsArrayEntity
import entities.response.PromotedTagsResponseEntity
import entities.response.TagsArrayEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIPromotedTagsArray
import io.square1.limor.uimodels.UIPromotedTagsResponse
import io.square1.limor.uimodels.UITagsArray
import io.square1.limor.uimodels.UITagsResponse


fun Single<PromotedTagsResponseEntity>.asUIModel(): Single<UIPromotedTagsResponse> {
    return this.map { it.asUIModel() }
}


fun PromotedTagsResponseEntity.asUIModel(): UIPromotedTagsResponse{
    return UIPromotedTagsResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun PromotedTagsArrayEntity.asUIModel(): UIPromotedTagsArray {
    return UIPromotedTagsArray(
        getAllUITags(promoted_tags)
    )
}
