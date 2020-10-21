package com.limor.app.di.modules

import dagger.Binds
import dagger.Module
import io.square1.limor.storage.providers.StorageDraftProviderImp
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.storage.StorageDraftProvider


@Module
abstract class StorageModule {

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

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindStorageDraftProvider(storageDraftProviderImpl: StorageDraftProviderImp): StorageDraftProvider
}