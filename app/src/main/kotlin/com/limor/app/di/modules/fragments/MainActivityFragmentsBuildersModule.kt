package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.*
import com.limor.app.scenes.main.fragments.discover.category.DiscoverAllCategoriesFragment
import com.limor.app.scenes.main.fragments.discover.category.DiscoverCategoryFragment
import com.limor.app.scenes.main.fragments.discover.discover.DiscoverFragment
import com.limor.app.scenes.main.fragments.discover.featuredcasts.DiscoverFeaturedCastsFragment
import com.limor.app.scenes.main.fragments.profile.JoinToPatronFragment
import com.limor.app.scenes.main.fragments.profile.UserLikedPodcastsFragment
import com.limor.app.scenes.main.fragments.profile.UserPatronPodcastsFragment
import com.limor.app.scenes.main.fragments.profile.UserPodcastsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeFragmentInjector(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedItemsListFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFragmentInjector(): DiscoverFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverCategoryFragmentInjector(): DiscoverCategoryFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFeaturedCastsFragmentInjector(): DiscoverFeaturedCastsFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverAllCategoriesFragmentInjector(): DiscoverAllCategoriesFragment

    //@ContributesAndroidInjector
    //abstract fun contributeRecordFragmentInjector(): RecordActivity

    @ContributesAndroidInjector
    abstract fun contributeNotificationsFragmentInjector(): NotificationsFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragmentInjector(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPodcastsFragmentInjector(): UserPodcastsFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFeedFragmentInjector(): UserFeedFragment

    @ContributesAndroidInjector
    abstract fun contributeUserLikedPodcastsFragmentInjector(): UserLikedPodcastsFragment

    @ContributesAndroidInjector
    abstract fun contributeJoinToPatronFragmentInjector(): JoinToPatronFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPatronPodcastsFragmentInjector(): UserPatronPodcastsFragment
}
