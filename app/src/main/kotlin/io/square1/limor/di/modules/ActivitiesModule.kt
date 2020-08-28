package io.square1.limor.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.di.modules.fragments.*
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.MainActivity
import io.square1.limor.scenes.main.fragments.podcast.PodcastDetailsActivity
import io.square1.limor.scenes.main.fragments.record.RecordActivity
import io.square1.limor.scenes.splash.SplashActivity

@Module
abstract class ActivitiesModule {

    @ContributesAndroidInjector
    abstract fun contributeSplashActivityInjector(): SplashActivity


    @ContributesAndroidInjector(modules = [
        (SignActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeSignActivityInjector(): SignActivity


    @ContributesAndroidInjector(modules = [
        (MainActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeMainActivityInjector(): MainActivity


    @ContributesAndroidInjector(modules = [
        (RecordActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeRecordActivityInjector(): RecordActivity


    @ContributesAndroidInjector(modules = [
        (PodcastDetailsActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributePodcastDetailsActivityInjector(): PodcastDetailsActivity


}