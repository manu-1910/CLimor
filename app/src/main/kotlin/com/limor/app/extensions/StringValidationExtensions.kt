package com.limor.app.extensions

import android.util.Patterns

const val PASSWORD_MIN_LENGTH = 5

fun String.isValidEmail(): Boolean = this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPassWord(): Boolean = this.isNotBlank() && this.count() >= PASSWORD_MIN_LENGTH