package com.limor.app.apollo

import timber.log.Timber

object TestTimberInstance {

    fun initTimber() {
        if(Timber.forest().isNotEmpty()) return
        Timber.plant(object : Timber.DebugTree() {
            override fun d(message: String?, vararg args: Any?) {
                println(message)
            }

            override fun e(t: Throwable?, message: String?, vararg args: Any?) {
                println(message)
                print("Stack : ${t?.stackTrace}")
            }
        })
    }
}