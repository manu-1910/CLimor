package com.limor.app.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import com.limor.app.scenes.main.fragments.FeedItemsListFragment
import com.limor.app.scenes.main.fragments.PodcastsByTagFragment

@Module
abstract class PodcastsByTagActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedItemsListFragment

    @ContributesAndroidInjector
    abstract fun contributePodcastsByTagFragmentInjector(): PodcastsByTagFragment
}