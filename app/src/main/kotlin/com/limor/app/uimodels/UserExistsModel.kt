package com.limor.app.uimodels

import android.os.Parcelable
import com.limor.app.GetUserByPhoneNumberQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserExistsModel(
    var isFound: Boolean?,
    val isDeleted: Boolean?
) : Parcelable

fun GetUserByPhoneNumberQuery.GetUserByPhoneNumber.mapToUIModel() =
    UserExistsModel(
        isFound = isFound,
        isDeleted = isDeleted
    )