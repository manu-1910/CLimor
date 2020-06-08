package io.square1.limor.storage.entities

import io.realm.RealmList
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
    var isEditMode: Boolean? = false,
    var timeStamps: RealmList<RLMTimeStamp>? = RealmList()
):RealmObject()



open class RLMTimeStamp(
    var duration: Int? = 0,
    var startSample: Int? = 0,
    var endSample: Int? = 0
):RealmObject()



