package com.limor.app.uimodels

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VerifyPromoCodeResult(
    val isDiscountCodeValid: Boolean,
    val priceId: String? = null
) : Parcelable