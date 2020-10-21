package com.limor.app.mappers

import com.limor.app.uimodels.UIPromotedTagsArray
import com.limor.app.uimodels.UIPromotedTagsResponse
import entities.response.PromotedTagsArrayEntity
import entities.response.PromotedTagsResponseEntity
import io.reactivex.Single


fun Single<PromotedTagsResponseEntity>.asUIModel(): Single<UIPromotedTagsResponse> {
    return this.map { it.asUIModel() }
}


fun PromotedTagsResponseEntity.asUIModel(): UIPromotedTagsResponse {
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
