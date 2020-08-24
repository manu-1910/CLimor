package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.podcast.PodcastDetailsFragment

@Module
abstract class PodcastDetailsActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributePodcastDetailsFragmentInjector(): PodcastDetailsFragment
}