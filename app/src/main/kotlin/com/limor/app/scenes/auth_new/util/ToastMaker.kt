package com.limor.app.scenes.auth_new.util

import android.content.Context
import android.widget.Toast

class ToastMaker {

    companion object{
        fun showToast(context: Context, message: Any){
            when (message) {
                is Int -> Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                is String -> Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}