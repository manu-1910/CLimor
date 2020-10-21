package com.limor.app.di.modules.fragments

import com.limor.app.common.SessionManager
import dagger.Binds
import dagger.Module

@Module
abstract class CommonFragmentsBuildersModule {

    @Binds
    abstract fun bindSessionManager(sessionManager: SessionManager): SessionManager

}