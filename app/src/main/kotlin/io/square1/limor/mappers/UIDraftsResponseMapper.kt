package io.square1.limor.mappers

import entities.response.DraftEntity
import entities.response.DraftsResponseEntity
import io.reactivex.Single
import io.square1.limor.uimodels.UIDraft
import io.square1.limor.uimodels.UIDraftsResponse


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
        audioDuration,
        audioStart,
        audioEnd
    )
}



fun Single<UIDraft>.asDataEntity(): Single<DraftEntity> {
    return this.map { it.asDataEntity() }
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
        audioDuration,
        audioStart,
        audioEnd
    )
}
