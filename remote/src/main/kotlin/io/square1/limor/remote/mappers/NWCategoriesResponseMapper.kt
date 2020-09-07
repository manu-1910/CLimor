package io.square1.limor.remote.mappers


import entities.response.CategoriesArrayEntity
import entities.response.CategoriesResponseEntity
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.NWCategoriesArray
import io.square1.limor.remote.entities.responses.NWCategoriesResponse


fun Single<NWCategoriesResponse>.asDataEntity(): Single<CategoriesResponseEntity>? {
    return this.map { it.asDataEntity() }
}


fun NWCategoriesResponse.asDataEntity(): CategoriesResponseEntity{
    return CategoriesResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}


fun NWCategoriesArray.asDataEntity(): CategoriesArrayEntity{
    return CategoriesArrayEntity(
        getAllCategoriesEntities(categories)
    )
}






