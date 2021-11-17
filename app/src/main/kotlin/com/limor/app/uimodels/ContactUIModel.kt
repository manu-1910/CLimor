package com.limor.app.uimodels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContactUIModel(
    val id: Int,
    val contactName: String?,
    val profilePhoto: String?,
    var checked: Boolean? = false,
): Parcelable