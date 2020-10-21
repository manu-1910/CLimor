package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PodcastDetailsActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributePodcastDetailsFragmentInjector(): PodcastDetailsFragment
}