package io.square1.limor.storage.entities

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RLMRecordItem(
    @PrimaryKey
    var id: Int = 0,
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,

    var minimum_term: String? = "",
    var notice_period: String? = "",
    var parking: Boolean? = false,
    var parking_price: String? = "",
    var phone_per_person: String? = "",
    var postcode: String? = "",
    var vat_charged: Boolean? = false,
    var currency: String? = "",
    var isAdditionalDetails: Boolean? = false,
    var isMeetingRooms: Boolean? = false,
    var isDraft: Boolean? = false
): RealmObject()
