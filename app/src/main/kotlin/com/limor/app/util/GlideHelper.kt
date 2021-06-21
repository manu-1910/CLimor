package com.limor.app.util

import android.widget.ImageView
import com.bumptech.glide.Glide

object GlideHelper {

    fun <T : ImageView> loadImageSimple(view: T, url: String) {
        Glide.with(view).load(url).into(view)
    }
}