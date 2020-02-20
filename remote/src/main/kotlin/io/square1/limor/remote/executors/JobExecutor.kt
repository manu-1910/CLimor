package io.square1.limor.remote.executors

import io.reactivex.schedulers.Schedulers
import io.square1.limor.data.executors.ThreadExecutor
import java.util.concurrent.*
import javax.inject.Inject

open class JobExecutor @Inject constructor() : ThreadExecutor {

    private val workQueue: BlockingQueue<Runnable>

    private val threadPoolExecutor: ThreadPoolExecutor

    private val threadFactory: ThreadFactory

    init {
        this.workQueue = LinkedBlockingQueue<Runnable>()
        this.threadFactory = JobThreadFactory()
        this.threadPoolExecutor = ThreadPoolExecutor(
            INITIAL_POOL_SIZE,
            MAX_POOL_SIZE, KEEP_ALIVE_TIME.toLong(),
            KEEP_ALIVE_TIME_UNIT,
            this.workQueue, this.threadFactory)
    }

    override fun getScheduler() = Schedulers.from(this)

    override fun execute(runnable: Runnable?) {
        runnable?.let { this.threadPoolExecutor.execute(runnable) }
            ?: throw IllegalArgumentException("Runnable to execute cannot be null")
    }

    private class JobThreadFactory : ThreadFactory {
        private var counter = 0

        override fun newThread(runnable: Runnable): Thread = Thread(runnable, THREAD_NAME + counter++)

        companion object {
            private val THREAD_NAME = "android_"
        }
    }

    companion object {

        private const val INITIAL_POOL_SIZE = 3
        private const val MAX_POOL_SIZE = 5

        // Sets the amount of time an idle thread waits before terminating
        private const val KEEP_ALIVE_TIME = 10

        // Sets the Time Unit to seconds
        private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    }
}