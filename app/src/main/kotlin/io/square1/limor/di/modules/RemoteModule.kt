package io.square1.limor.di.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.square1.limor.App
import io.square1.limor.BuildConfig
import io.square1.limor.common.SessionManager
import io.square1.limor.remote.providers.*
import io.square1.limor.remote.services.RemoteServiceConfig
import kotlinx.serialization.ImplicitReflectionSerializer
import providers.remote.*
import javax.inject.Singleton
import kotlin.math.absoluteValue

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
        private val sessionTime = if (sessionManager.getStoredSessionTime()!!.absoluteValue < 0) {
            0
        } else {
            sessionManager.getStoredSessionTime()
        }

        @Provides
        @JvmStatic
        @Singleton
        fun provideRemoteServiceConfig(): RemoteServiceConfig = RemoteServiceConfig(
            baseUrl = BuildConfig.BASE_URL,
            debug = BuildConfig.DEBUG,
            client_id = BuildConfig.CLIENT_ID,
            client_secret = BuildConfig.CLIENT_SECRET,
            token = tokenId,
            expiredIn = sessionTime!!
        )
    }

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindRemoteAuthProvider(remoteAuthProviderImp: RemoteAuthProviderImp): RemoteAuthProvider

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindRemoteUserProvider(remoteUserProviderImp: RemoteUserProviderImp): RemoteUserProvider

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindRemotePodcastProvider(remotePodcastProviderImp: RemotePodcastProviderImp): RemotePodcastProvider

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindRemoteSearchProvider(remoteSearchProviderImp: RemoteSearchProviderImp): RemoteSearchProvider

    @ImplicitReflectionSerializer
    @Binds
    abstract fun bindRemoteCategoriesProvider(remoteCategoriesProviderImp: RemoteCategoriesProviderImp): RemoteCategoriesProvider

   }