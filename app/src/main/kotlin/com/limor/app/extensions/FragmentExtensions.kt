package com.limor.app.extensions

import androidx.fragment.app.Fragment

fun Fragment.requireTag(): String {
    return tag ?: javaClass.name
}

fun Fragment.dismissFragment() {
    parentFragmentManager
        .beginTransaction()
        .remove(this)
        .commit()
}
