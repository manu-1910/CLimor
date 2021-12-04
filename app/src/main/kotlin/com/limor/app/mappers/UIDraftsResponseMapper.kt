package com.limor.app.mappers

import com.limor.app.uimodels.*
import entities.response.*
import io.reactivex.Single
import org.apache.commons.collections4.iterators.ArrayListIterator


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

fun getAllUICategories(uiList: List<OnDeviceCategoryEntity>?): ArrayList<UISimpleCategory> {
    return ArrayList(uiList?.map { it.asUIModel() } ?: listOf())
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
        languageCode,
        language,
        category,
        location?.asUIModel(),
        parentDraft?.asUIModel(),
        isNewRecording,
        getAllUICategories(categories)
    )
}


fun TimeStampEntity.asUIModel(): UITimeStamp {
    return UITimeStamp(
        duration,
        startSample,
        endSample
    )
}

fun OnDeviceCategoryEntity.asUIModel(): UISimpleCategory {
    return UISimpleCategory(name, categoryId)
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
        languageCode,
        language,
        category,
        location?.asDataEntity(),
        draftParent?.asDataEntity(),
        isNewRecording,
        getAllCategories(categories)
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

fun UISimpleCategory.asDataEntity(): OnDeviceCategoryEntity {
    return OnDeviceCategoryEntity(
        name,
        categoryId
    )
}

fun getAllCategories(uiList: List<UISimpleCategory>?): ArrayList<OnDeviceCategoryEntity> {
    return ArrayList(uiList?.map { it.asDataEntity() } ?: listOf())
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