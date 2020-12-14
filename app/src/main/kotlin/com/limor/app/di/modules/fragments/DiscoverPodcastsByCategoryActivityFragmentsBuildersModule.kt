package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.discover.DiscoverPodcastsByCategoryFragment
import com.limor.app.scenes.main.fragments.setup_patron.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DiscoverPodcastsByCategoryActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeDiscoverPodcastsByCategoryFragmentInjector(): DiscoverPodcastsByCategoryFragment
}