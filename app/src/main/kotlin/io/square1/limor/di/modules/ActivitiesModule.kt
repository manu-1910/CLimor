package io.square1.limor.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.di.modules.fragments.AuthenticationActivityFragmentsBuildersModule
import io.square1.limor.di.modules.fragments.CommonFragmentsBuildersModule
import io.square1.limor.scenes.authentication.AuthenticationActivity
import io.square1.limor.scenes.splash.SplashActivity

@Module
abstract class ActivitiesModule {
    @ContributesAndroidInjector(modules = [(CommonFragmentsBuildersModule::class), (AuthenticationActivityFragmentsBuildersModule::class)])
    abstract fun contributeAuthenticationActivityInjector(): AuthenticationActivity

    @ContributesAndroidInjector
    abstract fun contributeSplashActivityInjector(): SplashActivity
}