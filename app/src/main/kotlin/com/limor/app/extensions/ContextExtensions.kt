package com.limor.app.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

tailrec fun Context.getActivity(): Activity? = this as? Activity
    ?: (this as? ContextWrapper)?.baseContext?.getActivity()

fun Context.isOnline():Boolean{
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capability = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if(capability!=null){
        when{
            capability.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
            capability.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
            capability.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
        }
    }
    return false
}