package com.limor.app.mappers

import com.limor.app.uimodels.UIDraft
import com.limor.app.uimodels.UIDraftsResponse
import com.limor.app.uimodels.UILocations
import com.limor.app.uimodels.UITimeStamp
import entities.response.DraftEntity
import entities.response.DraftsResponseEntity
import entities.response.LocationsEntity
import entities.response.TimeStampEntity
import io.reactivex.Single


fun Single<DraftsResponseEntity>.asUIModel(): Single<UIDraftsResponse> {
    return this.map { it.asUIModel() }
}

fun DraftsResponseEntity.asUIModel(): UIDraftsResponse {
    return UIDraftsResponse(
        getAllUIDrafts(data),
        status
    )
}

fun getAllUIDrafts(entityList: ArrayList<DraftEntity>?): ArrayList<UIDraft> {
    val uiList = ArrayList<UIDraft>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun getAllUITimeStamps(entityList: ArrayList<TimeStampEntity>?): ArrayList<UITimeStamp> {
    val uiList = ArrayList<UITimeStamp>()
    if (entityList != null) {
        for (item in entityList) {
            if (item != null)
                uiList.add(item.asUIModel())
        }
    }
    return uiList
}

fun DraftEntity.asUIModel(): UIDraft {
    return UIDraft(
        id,
        title,
        caption,
        filePath,
        editedFilePath,
        tempPhotoPath,
        length,
        time,
        isEditMode,
        getAllUITimeStamps(timeStamps),
        date,
        categoryId,
        category,
        location?.asUIModel(),
        parentDraft?.asUIModel()
    )
}


fun TimeStampEntity.asUIModel(): UITimeStamp {
    return UITimeStamp(
        duration,
        startSample,
        endSample
    )
}



fun UIDraft.asDataEntity(): DraftEntity {
    return DraftEntity(
        id,
        title,
        caption,
        filePath,
        editedFilePath,
        tempPhotoPath,
        length,
        time,
        isEditMode,
        getAllTimeStamps(timeStamps),
        date,
        categoryId,
        category,
        location?.asDataEntity(),
        draftParent?.asDataEntity()
    )
}

fun UILocations.asDataEntity(): LocationsEntity{
    return LocationsEntity(
        address,
        latitude,
        longitude,
        isSelected
    )
}

fun UITimeStamp.asDataEntity(): TimeStampEntity{
    return TimeStampEntity(
        duration,
        startSample,
        endSample
    )
}


fun getAllTimeStamps(uiList: ArrayList<UITimeStamp>?): ArrayList<TimeStampEntity> {
    val entityList = ArrayList<TimeStampEntity>()
    if (uiList != null) {
        for (item in uiList) {
            if (item != null)
                entityList.add(item.asDataEntity())
        }
    }
    return entityList
}


fun Single<UITimeStamp>.asDataEntity(): Single<TimeStampEntity> {
    return this.map { it.asDataEntity() }
}