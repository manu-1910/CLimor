package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.FeedItemsListFragment
import com.limor.app.scenes.main.fragments.PodcastsByTagFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PodcastsByTagActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedItemsListFragment

    @ContributesAndroidInjector
    abstract fun contributePodcastsByTagFragmentInjector(): PodcastsByTagFragment
}