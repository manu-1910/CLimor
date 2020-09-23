package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.FeedItemsListFragment
import io.square1.limor.scenes.main.fragments.PodcastsByTagFragment

@Module
abstract class PodcastsByTagActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedItemsListFragment

    @ContributesAndroidInjector
    abstract fun contributePodcastsByTagFragmentInjector(): PodcastsByTagFragment
}