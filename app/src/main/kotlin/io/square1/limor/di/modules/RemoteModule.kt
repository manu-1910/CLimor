package io.square1.limor.di.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.square1.limor.App
import io.square1.limor.BuildConfig
import io.square1.limor.common.SessionManager
import io.square1.limor.remote.providers.RemoteAuthProviderImp
import io.square1.limor.remote.providers.RemoteUserProviderImp
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.RemoteAuthProvider
import providers.remote.RemoteUserProvider
import javax.inject.Singleton

@Module
abstract class RemoteModule {
    /**
     * This companion object annotated as a module is necessary in order to provide dependencies
     * statically in case the wrapping module is an abstract class (to use binding)
     */
    @Module
    companion object {

        private val sessionManager = SessionManager(App.instance)
        private val tokenId = if (sessionManager.getStoredToken().isNullOrEmpty()) {
            ""
        } else {
            sessionManager.getStoredToken().toString()
        }

        @Provides
        @JvmStatic
        @Singleton
        fun provideRemoteServiceConfig(): RemoteServiceConfig = RemoteServiceConfig(
            baseUrl = BuildConfig.BASE_URL,
            debug = BuildConfig.DEBUG,
            client_id = BuildConfig.CLIENT_ID,
            client_secret = BuildConfig.CLIENT_SECRET,
            token = tokenId
        )
    }

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindRemoteAuthProvider(remoteAuthProviderImp: RemoteAuthProviderImp): RemoteAuthProvider

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindRemoteUserProvider(remoteUserProviderImp: RemoteUserProviderImp): RemoteUserProvider

   }