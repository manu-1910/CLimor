package com.limor.app.di.modules.fragments

import dagger.Binds
import dagger.Module
import com.limor.app.common.SessionManager

@Module
abstract class CommonFragmentsBuildersModule {

    @Binds
    abstract fun bindSessionManager(sessionManager: SessionManager): SessionManager

}