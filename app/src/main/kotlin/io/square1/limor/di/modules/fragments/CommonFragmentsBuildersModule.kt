package io.square1.limor.di.modules.fragments

import dagger.Binds
import dagger.Module
import io.square1.limor.common.SessionManager

@Module
abstract class CommonFragmentsBuildersModule {

    @Binds
    abstract fun bindSessionManager(sessionManager: SessionManager): SessionManager

}