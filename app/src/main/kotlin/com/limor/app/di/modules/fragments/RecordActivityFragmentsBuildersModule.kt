package com.limor.app.di.modules.fragments


import com.limor.app.scenes.main.fragments.record.*
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class RecordActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeRecordFragmentInjector(): RecordFragment

    @ContributesAndroidInjector
    abstract fun contributeEditFragmentInjector(): EditFragment

    //@ContributesAndroidInjector
    //abstract fun contributeWaveformragmentInjector(): WaveformFragment

    @ContributesAndroidInjector
    abstract fun contributePublishFragmentInjector(): PublishFragment

    @ContributesAndroidInjector
    abstract fun contributeDraftsFragmentInjector(): DraftsFragment

    @ContributesAndroidInjector
    abstract fun contributeCategoriesFragmentInjector(): CategoriesFragment

    @ContributesAndroidInjector
    abstract fun contributeLocationsFragmentInjector(): LocationsFragment
}