package com.limor.app.mappers

import com.limor.app.uimodels.UITagsArray
import com.limor.app.uimodels.UITagsResponse
import entities.response.TagsArrayEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single


fun Single<TagsResponseEntity>.asUIModel(): Single<UITagsResponse> {
    return this.map { it.asUIModel() }
}


fun TagsResponseEntity.asUIModel(): UITagsResponse {
    return UITagsResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun TagsArrayEntity.asUIModel(): UITagsArray {
    return UITagsArray(
        getAllUITags(tags)
    )
}
