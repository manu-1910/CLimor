package com.limor.app.mappers

import com.limor.app.uimodels.UILocations
import com.limor.app.uimodels.UILocationsArray
import com.limor.app.uimodels.UILocationsResponse
import entities.response.LocationsEntity
import entities.response.LocationsEntityArray
import entities.response.LocationsResponseEntity
import io.reactivex.Single


fun Single<LocationsResponseEntity>.asUIModel(): Single<UILocationsResponse> {
    return this.map { it.asUIModel() }
}


fun LocationsResponseEntity.asUIModel(): UILocationsResponse {
    return UILocationsResponse(
        code,
        message,
        data.asUIModel()
    )
}


fun LocationsEntityArray.asUIModel(): UILocationsArray {
    return UILocationsArray(
        getAllUILocations(locations)
    )
}


fun LocationsEntity.asUIModel(): UILocations {
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
