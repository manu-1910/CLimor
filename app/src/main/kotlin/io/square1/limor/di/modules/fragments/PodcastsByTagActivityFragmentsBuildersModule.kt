package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.FeedFragment

@Module
abstract class PodcastsByTagActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedFragment
}