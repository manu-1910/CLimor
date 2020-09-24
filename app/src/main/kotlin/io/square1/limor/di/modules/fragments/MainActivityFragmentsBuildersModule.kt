package io.square1.limor.di.modules.fragments


import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.*
import io.square1.limor.scenes.main.fragments.profile.UserPodcastsFragment


@Module
abstract class MainActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeFragmentInjector(): HomeFragment

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedItemsListFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFragmentInjector(): DiscoverFragment

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
    abstract fun contributeDiscoverAccountsFragmentInjector(): DiscoverAccountsFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverHashTagsFragmentInjector(): DiscoverHashTagsFragment

}