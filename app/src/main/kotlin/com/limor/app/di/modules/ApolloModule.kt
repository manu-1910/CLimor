package com.limor.app.di.modules

import com.apollographql.apollo.ApolloClient
import com.limor.app.apollo.Apollo
import com.limor.app.apollo.ApolloImpl
import com.limor.app.apollo.GRAPHQL_ENDPOINT
import com.limor.app.apollo.interceptors.AuthInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApolloModule {

    @Provides
    @Singleton
    fun provideApollo(apolloClient: ApolloClient): Apollo {
        return ApolloImpl(apolloClient)
    }

    @Provides
    @Singleton
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.builder()
            .serverUrl(GRAPHQL_ENDPOINT)
            .okHttpClient(
                okHttpClient
            )
            .build()
    }

    @Provides
    @Named("OkhttpClientForApolloInterceptors")
    @Singleton
    fun providesOkhttpClientInterceptorsForApollo(): List<@JvmSuppressWildcards Interceptor> {
        return listOf(AuthInterceptor())
    }

    @Provides
    @Singleton
    fun provideOkhttpClientForApollo(@Named("OkhttpClientForApolloInterceptors") interceptors: List<@JvmSuppressWildcards Interceptor>)
            : OkHttpClient {
        val builder = OkHttpClient.Builder()
        interceptors.forEach { builder.addInterceptor(it) }
        return builder.build()
    }
}