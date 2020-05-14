package io.square1.limor.mappers

import entities.response.DraftEntity
import entities.response.DraftsResponseEntity
import entities.response.TimeStampEntity
import io.reactivex.Single
import io.square1.limor.scenes.utils.waveform.WaveformFragment.isEditMode
import io.square1.limor.uimodels.UIDraft
import io.square1.limor.uimodels.UIDraftsResponse
import io.square1.limor.uimodels.UITimeStamp


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
        getAllUITimeStamps(timeStamps)
    )
}


fun TimeStampEntity.asUIModel(): UITimeStamp{
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
        getAllTimeStamps(timeStamps)
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