package com.limor.app.di.modules

import android.app.Application
import android.content.Context
import com.limor.app.common.dispatchers.DispatcherProvider
import com.limor.app.common.dispatchers.DispatcherProviderImpl
import com.limor.app.common.executors.PostExecutionThread
import com.limor.app.common.executors.UIThread
import dagger.Binds
import dagger.Module
import io.square1.limor.data.executors.ThreadExecutor
import io.square1.limor.remote.executors.JobExecutor

@Module
abstract class ApplicationModule {

    /**
     * This companion object annotated as a module is necessary in order to provide dependencies
     * statically in case the wrapping module is an abstract class (to use binding)
     */
    @Module
    companion object {
        /**
        @Provides
        @JvmStatic
        fun provideSomething(): Something {
        return InstanceOfSomething
        }*/
    }

    @Binds
    abstract fun bindApplicationContext(application: Application): Context

    @Binds
    abstract fun bindThreadExecutor(jobExecutor: JobExecutor): ThreadExecutor

    @Binds
    abstract fun bindPostExecutionThread(uiThread: UIThread): PostExecutionThread

    abstract fun bindDispatchersProvider(dispatcherProvider: DispatcherProviderImpl): DispatcherProvider
}