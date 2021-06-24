package com.limor.app.util

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.loadUrlSimple(url: String) {
    Glide.with(this).load(url).into(this)
}