package com.limor.app.scenes.auth_new.util

import android.content.Context
import android.content.res.ColorStateList
import androidx.appcompat.content.res.AppCompatResources

fun colorStateList(context: Context, boxStrokeColor: Int): ColorStateList {
    return AppCompatResources.getColorStateList(context, boxStrokeColor)
}