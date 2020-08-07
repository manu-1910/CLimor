package io.square1.limor.mappers

import entities.response.*
import io.reactivex.Single
import io.square1.limor.uimodels.*


fun Single<LocationsResponseEntity>.asUIModel(): Single<UILocationsResponse> {
    return this.map { it.asUIModel() }
}


fun LocationsResponseEntity.asUIModel(): UILocationsResponse{
    return UILocationsResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun LocationsEntityArray.asUIModel(): UILocationsArray{
    return UILocationsArray(
        getAllUILocations(locations)
    )
}


fun LocationsEntity.asUIModel(): UILocations{
    return UILocations(
        address,
        latitude,
        longitude,
        isSelected
    )
}


fun getAllUILocations(entityList: ArrayList<LocationsEntity>?): ArrayList<UILocations> {
    val uiList = ArrayList<UILocations>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}
