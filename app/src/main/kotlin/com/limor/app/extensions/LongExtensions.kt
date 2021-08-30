package com.limor.app.extensions

import kotlin.math.log10
import kotlin.math.pow

val Long.formatHumanReadable: String
    get() {
        if (this < 1000) {
            return "$this"
        }
        return log10(toDouble()).toInt().div(3).let {
            val precision = when (it) {
                0 -> 0; else -> 1
            }
            val suffix = arrayOf("", "k", "m", "g", "t", "p", "e", "z", "y")
            String.format("%.${precision}f${suffix[it]}", toDouble() / 10.0.pow(it * 3))
        }
    }