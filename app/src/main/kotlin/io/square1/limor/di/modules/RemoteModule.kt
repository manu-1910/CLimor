package io.square1.limor.di.modules

import dagger.Module
import dagger.Provides
import io.square1.limor.BuildConfig
import io.square1.limor.remote.services.RemoteServiceConfig
import javax.inject.Singleton

@Module
abstract class RemoteModule {
    /**
     * This companion object annotated as a module is necessary in order to provide dependencies
     * statically in case the wrapping module is an abstract class (to use binding)
     */
    @Module
    companion object {
        @Provides
        @JvmStatic
        @Singleton
        fun provideRemoteServiceConfig(): RemoteServiceConfig = RemoteServiceConfig(
            apiKey = BuildConfig.API_KEY,
            baseUrl = BuildConfig.BASE_URL,
            appVersion = "test-version",
            debug = BuildConfig.DEBUG
        )
    }

   }