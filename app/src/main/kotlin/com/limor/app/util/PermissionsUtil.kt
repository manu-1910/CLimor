package com.limor.app.util

import android.Manifest
import android.app.Activity
import android.content.Context
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

private val RECORD_PERMISSIONS = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

val STORAGE_PERMISSIONS = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
)

const val RECORD_PERMISSIONS_REQUEST_ID = 1001
const val STORAGE_PERMISSIONS_REQUEST_ID = 2001

fun hasRecordPermissions(context: Context): Boolean = RECORD_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun hasStoragePermissions(context: Context): Boolean = STORAGE_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun requestStoragePermissions(activity: Activity) {
    try {
        ActivityCompat.requestPermissions(
            activity,
            STORAGE_PERMISSIONS,
            STORAGE_PERMISSIONS_REQUEST_ID
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun requestRecordPermissions(activity: Activity) {
    try {
        ActivityCompat.requestPermissions(
            activity,
            RECORD_PERMISSIONS,
            RECORD_PERMISSIONS_REQUEST_ID
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}