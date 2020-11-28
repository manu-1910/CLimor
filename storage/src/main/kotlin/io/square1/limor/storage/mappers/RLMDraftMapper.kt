package io.square1.limor.storage.mappers

import entities.response.DraftEntity
import entities.response.LocationsEntity
import entities.response.TimeStampEntity
import io.square1.limor.storage.entities.RLMDraft
import io.reactivex.Single
import io.realm.RealmList
import io.square1.limor.storage.entities.RLMLocations
import io.square1.limor.storage.entities.RLMTimeStamp


fun RLMDraft.asDataEntity(): DraftEntity {
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
        getTimeStampsEntities(timeStamps),
        date,
        categoryId,
        category,
        location?.asDataEntity(),
        parentDraft?.asDataEntity(),
        isNewRecording
    )
}

fun Single<RLMDraft>.asDataEntity(): Single<DraftEntity> {
    return this.map { it.asDataEntity() }
}

fun DraftEntity.asStorageEntity(): RLMDraft {
    return RLMDraft(
        id,
        title,
        caption,
        filePath,
        editedFilePath,
        tempPhotoPath,
        length,
        time,
        isEditMode,
        getRLMTimeStamps(timeStamps),
        date,
        categoryId,
        category,
        location?.asStorageEntity(),
        parentDraft?.asStorageEntity(),
        isNewRecording
    )
}


fun RLMTimeStamp.asDataEntity(): TimeStampEntity {
    return TimeStampEntity(
        duration,
        startSample,
        endSample
    )
}


fun TimeStampEntity.asStorageEntity(): RLMTimeStamp {
    return RLMTimeStamp(
        duration,
        startSample,
        endSample
    )
}

fun LocationsEntity.asStorageEntity(): RLMLocations {
    return RLMLocations(
        address,
        latitude,
        longitude,
        isSelected
    )
}


fun RLMLocations.asDataEntity(): LocationsEntity {
    return LocationsEntity(
        address,
        latitude,
        longitude,
        isSelected
    )
}



fun getTimeStampsEntities(realmObj: RealmList<RLMTimeStamp>?): ArrayList<TimeStampEntity> {
    val entityObj = ArrayList<TimeStampEntity>()
    if(realmObj != null)
        for (option in realmObj) {
            entityObj.add(option.asDataEntity())
        }
    return entityObj
}


fun getRLMTimeStamps(entityObj: ArrayList<TimeStampEntity>?): RealmList<RLMTimeStamp> {
    val rlmObj = RealmList<RLMTimeStamp>()
    if (entityObj != null)
        for (option in entityObj) {
            rlmObj.add(option.asStorageEntity())
        }
    return rlmObj
}