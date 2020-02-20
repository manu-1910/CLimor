package io.square1.limor.common.executors

import io.reactivex.Scheduler

interface PostExecutionThread {
    fun getScheduler(): Scheduler
}