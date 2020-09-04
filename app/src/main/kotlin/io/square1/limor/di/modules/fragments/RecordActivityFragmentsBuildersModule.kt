package io.square1.limor.di.modules.fragments


import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.record.*


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