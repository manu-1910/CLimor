package com.limor.app.extensions

inline fun <T> List<T>.forEachIterable(block: (T) -> Unit) {
    with(listIterator()) {
        while (hasNext()) {
            block(next())
        }
    }
}

fun <T> List<T>.copyOf(): List<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}

fun <T> List<T>.mutableCopyOf(): MutableList<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}