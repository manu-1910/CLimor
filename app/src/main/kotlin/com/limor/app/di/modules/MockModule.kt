package com.limor.app.di.modules

import dagger.Module

@Module
abstract class MockModule {
    /**
     * This companion object annotated as a module is necessary in order to provide dependencies
     * statically in case the wrapping module is an abstract class (to use binding)
     */
    @Module
    companion object {
    }

}