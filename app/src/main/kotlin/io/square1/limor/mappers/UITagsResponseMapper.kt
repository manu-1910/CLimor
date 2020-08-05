package io.square1.limor.mappers

import entities.response.TagsArrayEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UITagsArray
import io.square1.limor.uimodels.UITagsResponse


fun Single<TagsResponseEntity>.asUIModel(): Single<UITagsResponse> {
    return this.map { it.asUIModel() }
}


fun TagsResponseEntity.asUIModel(): UITagsResponse{
    return UITagsResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun TagsArrayEntity.asUIModel(): UITagsArray{
    return UITagsArray(
        getAllUITags(tags)
    )
}



/**
//
data class UITagsResponse(
    val code: Int,
    val message: String,
    val data: UITagsArray
)

data class UITagsArray(
    val tags: ArrayList<UITags>
)
// */