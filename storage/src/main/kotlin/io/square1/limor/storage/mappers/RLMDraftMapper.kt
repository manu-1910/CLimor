package io.square1.limor.storage.mappers

import entities.response.DraftEntity
import io.square1.limor.storage.entities.RLMDraft
import io.reactivex.Single


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
        audioDuration,
        audioStart,
        audioEnd,
        isEditMode
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
        audioDuration,
        audioStart,
        audioEnd,
        isEditMode
    )
}

