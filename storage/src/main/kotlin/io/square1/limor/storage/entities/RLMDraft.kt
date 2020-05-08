package io.square1.limor.storage.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RLMDraft(
    @PrimaryKey
    var id: Long? = 0,
    var title: String? = "",
    var caption: String? = "",
    var filePath: String? = "",
    var editedFilePath: String? = "",
    var tempPhotoPath: String? = "",
    var length: Long? = 0,
    var time: Long? = 0,
    var audioDuration: Int? = 0,
    var audioStart: Int? = 0,
    var audioEnd: Int? = 0,
    var isEditMode: Boolean? = false
):RealmObject()