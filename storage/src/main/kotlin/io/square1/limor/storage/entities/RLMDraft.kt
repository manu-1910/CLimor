package io.square1.limor.storage.entities

import entities.response.CategoriesArrayEntity
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RLMDraft(
    @PrimaryKey var id: Long? = 0,
    var title: String? = "",
    var caption: String? = "",
    var filePath: String? = "",
    var editedFilePath: String? = "",
    var tempPhotoPath: String? = "",
    var length: Long? = 0,
    var time: Long? = 0,
    var isEditMode: Boolean? = false,
    var timeStamps: RealmList<RLMTimeStamp>? = RealmList(),
    var date: String? = "",
    var categoryId: Int? = 0,
    var languageCode: String? = "",
    var language: String? = "",
    var category: String? = "",
    var location: RLMLocations? = RLMLocations(),
    var parentDraft : RLMDraft? = null,
    var isNewRecording: Boolean = false,
    var categories: RealmList<RLMOnDeviceCategory>? = RealmList(),
    var price: String? = null
):RealmObject()



open class RLMTimeStamp(
    var duration: Int? = 0,
    var startSample: Int? = 0,
    var endSample: Int? = 0
):RealmObject()



open class RLMLocations(
    var address: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null,
    var isSelected: Boolean = false
):RealmObject()



open class RLMOnDeviceCategory(
    var name: String = "",
    var categoryId: Int = 0
): RealmObject()