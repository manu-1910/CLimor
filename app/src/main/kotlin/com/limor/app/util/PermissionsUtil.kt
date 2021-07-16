package com.limor.app.util

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Activity.checkPermission(permission: String, PERMISSION_REQUEST: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= 23
            && ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST)
            false
        } else true
    }