package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.Subcomponent
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.FeedItemsListFragment
import io.square1.limor.scenes.main.fragments.ProfileFragment
import io.square1.limor.scenes.main.fragments.profile.UserLikedPodcastsFragment
import io.square1.limor.scenes.main.fragments.profile.UserPodcastsFragment

@Module
abstract class UserProfileActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragmentInjector(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedItemsListFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPodcastsFragmentInjector(): UserPodcastsFragment

    @ContributesAndroidInjector
    abstract fun contributeUserLikedPodcastsFragmentInjector(): UserLikedPodcastsFragment
}

