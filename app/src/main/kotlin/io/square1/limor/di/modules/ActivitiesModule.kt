package io.square1.limor.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.di.modules.fragments.CommonFragmentsBuildersModule
import io.square1.limor.di.modules.fragments.MainActivityFragmentsBuildersModule
import io.square1.limor.di.modules.fragments.RecordActivityFragmentsBuildersModule
import io.square1.limor.di.modules.fragments.SignActivityFragmentsBuildersModule
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.MainActivity
import io.square1.limor.scenes.main.fragments.record.RecordActivity
import io.square1.limor.scenes.splash.SplashActivity

@Module
abstract class ActivitiesModule {

    @ContributesAndroidInjector
    abstract fun contributeSplashActivityInjector(): SplashActivity


    @ContributesAndroidInjector(modules = [
        (CommonFragmentsBuildersModule::class),
        (SignActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeSignActivityInjector(): SignActivity


    @ContributesAndroidInjector(modules = [
        (CommonFragmentsBuildersModule::class),
        (MainActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeMainActivityInjector(): MainActivity


    @ContributesAndroidInjector(modules = [
        (CommonFragmentsBuildersModule::class),
        (RecordActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeRecordActivityInjector(): RecordActivity


}