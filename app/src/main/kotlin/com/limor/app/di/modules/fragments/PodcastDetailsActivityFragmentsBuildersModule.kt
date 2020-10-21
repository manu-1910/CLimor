package com.limor.app.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsFragment

@Module
abstract class PodcastDetailsActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributePodcastDetailsFragmentInjector(): PodcastDetailsFragment
}