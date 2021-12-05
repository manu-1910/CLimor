package com.limor.app.scenes.auth_new.firebase

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.limor.app.scenes.auth_new.util.PrefsHandler

object FirebaseSessionHandler {

    fun logout(context: Context){
        PrefsHandler.clearNavigationBreakPoint(context)
        PrefsHandler.saveCurrentUserId(context, 0)
        Firebase.auth.signOut()
    }
}