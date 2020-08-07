package io.square1.limor.remote.mappers


import entities.response.*
import io.reactivex.Single
import io.square1.limor.remote.entities.responses.*


fun Single<NWLocationsResponse>.asDataEntity(): Single<LocationsResponseEntity>? {
    return this.map { it.asDataEntity() }
}


fun NWLocationsResponse.asDataEntity(): LocationsResponseEntity{
    return LocationsResponseEntity(
        code,
        message,
        data.asDataEntity()
    )
}



fun NWLocationsArray.asDataEntity(): LocationsEntityArray{
    return LocationsEntityArray(
        getAllLocationsEntities(locations)
    )
}


fun NWLocations.asDataEntity(): LocationsEntity{
    return LocationsEntity(
        address,
        latitude,
        longitude,
        isSelected
    )
}



fun getAllLocationsEntities(nwList: ArrayList<NWLocations>?): ArrayList<LocationsEntity> {
    val entityList = ArrayList<LocationsEntity>()
    if (nwList != null) {
        for (item in nwList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}




