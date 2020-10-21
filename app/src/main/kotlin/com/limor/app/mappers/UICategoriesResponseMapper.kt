package com.limor.app.mappers

import com.limor.app.uimodels.UICategoriesArray
import com.limor.app.uimodels.UICategoriesResponse
import entities.response.CategoriesArrayEntity
import entities.response.CategoriesResponseEntity
import io.reactivex.Single


fun Single<CategoriesResponseEntity>.asUIModel(): Single<UICategoriesResponse> {
    return this.map { it.asUIModel() }
}


fun CategoriesResponseEntity.asUIModel(): UICategoriesResponse {
    return UICategoriesResponse(
        code,
        message,
        data.asUIModel()
    )
}

fun CategoriesArrayEntity.asUIModel(): UICategoriesArray {
    return UICategoriesArray(
        getAllUICategory(categories)
    )
}


