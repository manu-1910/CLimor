package io.square1.limor.mappers

import entities.response.CategoriesArrayEntity
import entities.response.CategoriesResponseEntity
import entities.response.TagsArrayEntity
import entities.response.TagsResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UICategoriesArray
import io.square1.limor.uimodels.UICategoriesResponse
import io.square1.limor.uimodels.UITagsArray
import io.square1.limor.uimodels.UITagsResponse


fun Single<CategoriesResponseEntity>.asUIModel(): Single<UICategoriesResponse> {
    return this.map { it.asUIModel() }
}


fun CategoriesResponseEntity.asUIModel(): UICategoriesResponse{
    return UICategoriesResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun CategoriesArrayEntity.asUIModel(): UICategoriesArray{
    return UICategoriesArray(
        getAllUICategory(categories)
    )
}


