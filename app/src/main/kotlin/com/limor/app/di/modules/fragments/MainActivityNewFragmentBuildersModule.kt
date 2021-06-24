package com.limor.app.di.modules.fragments


import com.limor.app.scenes.main_new.fragments.FragmentHomeNew
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class MainActivityNewFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeFragmentInjector(): FragmentHomeNew

}

